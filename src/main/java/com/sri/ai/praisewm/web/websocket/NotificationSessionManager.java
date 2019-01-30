package com.sri.ai.praisewm.web.websocket;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.event.SessionStartedEvent;
import com.sri.ai.praisewm.event.notification.DataRefreshEvent;
import com.sri.ai.praisewm.event.notification.DataRefreshEvent.RefreshType;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.SessionCloseEvent;
import com.sri.ai.praisewm.event.notification.SessionLogoutEvent;
import com.sri.ai.praisewm.event.notification.SessionTimeoutEvent;
import com.sri.ai.praisewm.service.SecurityService;
import com.sri.ai.praisewm.service.SessionInfo;
import java.time.Instant;
import java.util.Arrays;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

/** NotificationSessionManager */
@WebSocket
public class NotificationSessionManager extends WebSocketSessionManager {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationSessionManager.class);
  private static final String ENDPOINT_SUFFIX = "notification";
  private final SecurityService securityService;
  private final EventBus eventBus;

  public NotificationSessionManager(
      Service sparkService, EventBus eventBus, SecurityService securityService) {
    super(sparkService, ENDPOINT_SUFFIX);
    this.securityService = securityService;
    eventBus.register(new NotificationEventListener());
    this.eventBus = eventBus;
  }

  public void broadcastEvent(NotificationEvent event) {
    final NotificationEvent.Broadcast action =
        event.getBroadcast() == null ? Broadcast.INCLUSIVE : event.getBroadcast();

    Arrays.stream(getSessionMap().values().toArray(new Session[0]))
        .filter(
            s ->
                action == Broadcast.INCLUSIVE
                    || !getSessionMap().inverse().get(s).equals(event.getSessionId()))
        .forEach(s -> sendMessage(s, event));
  }

  protected void afterOnConnect(Session session) {
    String sessionId = getSessionId(session);
    SessionInfo sessionInfo = securityService.getSessionMap().get(sessionId);
    if (sessionInfo == null) {
      LOG.error(
          "SessionId for session that just connected to webSocket not found in Security Service: id={}",
          sessionId);
      return;
    }
    sessionInfo.setWsLastOpenedTime(Instant.now());
    eventBus.post(new SessionStartedEvent(sessionId));
    broadcastDataRefreshEvent(sessionInfo, "Session started");
  }

  @Override
  protected void afterOnClose(Session session, int statusCode, String reason) {
    String sessionId = getSessionId(session);
    SessionInfo sessionInfo = securityService.getSessionMap().get(sessionId);

    // Session may have already been removed, so, don't log a message if not found
    if (sessionInfo != null) {
      sessionInfo.setWsLastClosedTime(Instant.now());
    }

    broadcastDataRefreshEvent(sessionInfo, "Session closed");
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    LOG.warn(
        "Unexpected websocket client message received. {}, Message={}",
        getFormattedWsSessionInfo(session),
        message);
  }

  private void broadcastDataRefreshEvent(SessionInfo sessionInfo, String reason) {
    String sessionId = "";
    String userMsg = "";

    if (sessionInfo != null) {
      sessionId = sessionInfo.getSessionId();
      userMsg = String.format(" : User: %s", sessionInfo.getUserName());
    }
    DataRefreshEvent dre = new DataRefreshEvent();
    dre.setRefreshType(RefreshType.USER);
    dre.setSessionId(sessionId);
    dre.setBroadcast(Broadcast.EXCLUSIVE);
    dre.setLevel(Level.INFO);
    dre.setText(
        String.format(
            "Data refresh. Type=%s, Reason: %s%s",
            dre.getRefreshType().toString().toLowerCase(), reason, userMsg));
    broadcastEvent(dre);
  }

  private class NotificationEventListener {
    @Subscribe
    public void sendNotificationEvent(NotificationEvent notificationEvent) {
      boolean isSessionClose = notificationEvent instanceof SessionCloseEvent;
      boolean isSessionTimeout = notificationEvent instanceof SessionTimeoutEvent;
      boolean isSessionLogout = notificationEvent instanceof SessionLogoutEvent;
      boolean isDataRefreshEvent = notificationEvent instanceof DataRefreshEvent;
      Session session;

      if (isSessionClose) {
        session = getSessionMap().remove(notificationEvent.getSessionId());
      } else {
        session = getSessionMap().get(notificationEvent.getSessionId());
      }

      if (session != null) {
        if (!isSessionLogout) {
          if (notificationEvent.getBroadcast() != null) {
            broadcastEvent(notificationEvent);
          } else {
            sendMessage(session, notificationEvent);
          }
        }

        if (isSessionClose) {
          session.close(
              isSessionTimeout ? StatusCode.TRY_AGAIN_LATER : StatusCode.NORMAL,
              ((SessionCloseEvent) notificationEvent).getText());
        }
      } else if (notificationEvent.getBroadcast() == Broadcast.EXCLUSIVE) {
        // A broadcast event may be initiated from a service
        // For example: the proto-server will not have a websocket session and neither will
        // the SystemStatusService.
        broadcastEvent(notificationEvent);
      } else {
        LOG.warn("Received event for a non-existent session: {}", notificationEvent);
      }
    }
  }
}
