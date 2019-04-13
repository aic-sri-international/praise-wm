package com.sri.ai.praisewm.web.ws;

import static com.sri.ai.praisewm.web.ws.WebsocketUtil.getFormattedWsSessionInfo;

import com.sri.ai.praisewm.service.SecurityServiceImpl;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The WsSessionStore manages the {@link WsSessionWrapper}.
 *
 * <p>The {@code server.ws.refresh.minIntervalInMillis} passed to each {@link WsSessionWrapper} is
 * obtained from the {@code application.properties} file.
 */
public class WsSessionStore {
  private static final Logger LOG = LoggerFactory.getLogger(WsSessionStore.class);
  private final long refreshEventInterval;
  private Map<String, WsSessionWrapper> sessionMap = new HashMap<>();

  public WsSessionStore(PropertiesWrapper pw) {
    this.refreshEventInterval = pw.asLong("server.ws.refresh.minIntervalInMillis");
  }

  // Call upon receipt of the websocket API onConnect call
  String onConnect(Session session) {
    String sessionId = WebsocketUtil.getSessionId(session);
    if (sessionId == null) {
      String err =
          String.format(
              "%s not found as a query parameter for websocket session onConnect",
              SecurityServiceImpl.SECURITY_HEADER_KEY);
      session.close(StatusCode.POLICY_VIOLATION, err);
    } else {
      WsSessionWrapper existingSessionWrapper = sessionMap.remove(sessionId);

      if (existingSessionWrapper != null) {
        String reason =
            String.format(
                "Closing old session to switch to new session %s",
                SecurityServiceImpl.SECURITY_HEADER_KEY);
        existingSessionWrapper.close(WsStatusCode.NORMAL, reason);
      }
      sessionMap.put(sessionId, new WsSessionWrapper(session, sessionId, refreshEventInterval));

      LOG.info(
          "New websocket session established: current total sessions={} : {}",
          sessionMap.size(),
          getFormattedWsSessionInfo(session));
    }

    return sessionId;
  }

  void close(String sessionId, WsStatusCode wsStatusCode, String message) {
    WsSessionWrapper wsSessionWrapper = sessionMap.remove(sessionId);
    LOG.info(
        "close called for sessionId={}, total sessions={}, session already remove={}",
        sessionId,
        sessionMap.size(),
        wsSessionWrapper == null);

    if (wsSessionWrapper != null) {
      wsSessionWrapper.close(wsStatusCode, message);
    }
  }

  // Call upon receipt of the websocket API onClose
  String onClose(Session session, int statusCode, String reason) {
    String sessionId = WebsocketUtil.getSessionId(session);
    boolean isMap = false;

    // If closure was initiated calling our close method, the sessionId will be null
    if (sessionId != null) {
      WsSessionWrapper wsSessionWrapper = sessionMap.remove(sessionId);
      if (wsSessionWrapper != null) {
        isMap = true;
        wsSessionWrapper.stop();
      }
    }
    LOG.info(
        "Session closed: InMap={}, Current total sessions={}, WebSocket StatusCode={}, Reason={}, {}",
        isMap,
        sessionMap.size(),
        statusCode,
        reason,
        getFormattedWsSessionInfo(session));

    return sessionId;
  }

  void sendMessage(String sessionId, Object message) {
    WsSessionWrapper wsSessionWrapper = sessionMap.get(sessionId);
    if (wsSessionWrapper != null) {
      wsSessionWrapper.sendMessage(message);
    } else {
      LOG.warn(
          "Received message for a non-existent session: sessionId={} message={}",
          sessionId,
          message);
    }
  }

  List<String> getSessionIds() {
    return new ArrayList<>(sessionMap.keySet());
  }
}
