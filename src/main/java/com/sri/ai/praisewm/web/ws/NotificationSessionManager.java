package com.sri.ai.praisewm.web.ws;

import static com.sri.ai.praisewm.web.ws.WebsocketUtil.getFormattedWsSessionInfo;

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
import com.sri.ai.praisewm.util.PropertiesWrapper;
import java.time.Instant;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

/**
 * The NotificationSessionManager extends the {@link WebSocketSessionManager} to handle Notification
 * events.
 *
 * <p>The NotificationSessionManager will post a {@link SessionStartedEvent} to notify other
 * services when UI client has establised their WebSocket connection.
 *
 * <p>UI clients are notified when a new client has either connected or disconnected by sending a
 * {@link DataRefreshEvent} of {@link RefreshType#USER}.
 */
@WebSocket
public class NotificationSessionManager extends WebSocketSessionManager {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationSessionManager.class);
  private static final String ENDPOINT_SUFFIX = "notification";
  private final SecurityService securityService;
  private final EventBus eventBus;

  public NotificationSessionManager(
      PropertiesWrapper pw,
      Service sparkService,
      EventBus eventBus,
      SecurityService securityService) {
    super(pw, sparkService, ENDPOINT_SUFFIX);
    this.securityService = securityService;
    eventBus.register(new NotificationEventListener());
    this.eventBus = eventBus;
  }

  public void broadcastEvent(NotificationEvent event) {
    final NotificationEvent.Broadcast action =
        event.getBroadcast() == null ? Broadcast.INCLUSIVE : event.getBroadcast();

    getSessionIds().stream()
        .filter(
            sessionId -> action == Broadcast.INCLUSIVE || !sessionId.equals(event.getSessionId()))
        .forEach(sessionId -> sendMessage(sessionId, event));
  }

  protected void afterOnConnect(String sessionId) {
    SessionInfo sessionInfo = securityService.getSessionMap().get(sessionId);
    if (sessionInfo == null) {
      // This should never really happen
      LOG.error(
          "SessionId for session that just connected to webSocket not found in Security Service: id={}",
          sessionId);
      close(
          sessionId,
          WsStatusCode.NORMAL,
          "afterOnConnect sessionId not not found in Security Service");
      return;
    }
    sessionInfo.setWsLastOpenedTime(Instant.now());
    eventBus.post(new SessionStartedEvent(sessionId));
    broadcastDataRefreshEvent(sessionInfo, "Session started");
  }

  @Override
  protected void afterOnClose(String sessionId, int statusCode, String reason) {
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

      String sessionId = notificationEvent.getSessionId();

      if (!isSessionLogout) {
        if (notificationEvent.getBroadcast() != null) {
          broadcastEvent(notificationEvent);
        } else {
          sendMessage(sessionId, notificationEvent);
        }
      }

      if (isSessionClose) {
        String msg = ((SessionCloseEvent) notificationEvent).getText();
        close(sessionId, isSessionTimeout ? WsStatusCode.TIMEOUT : WsStatusCode.NORMAL, msg);
      }
    }
  }
}
