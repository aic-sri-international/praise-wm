package com.sri.ai.praisewm.web.websocket;

import static com.sri.ai.praisewm.web.websocket.WebSocketConstants.ENDPOINT_PREFIX;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sri.ai.praisewm.service.SecurityServiceImpl;
import com.sri.ai.praisewm.util.JsonConverter;
import org.eclipse.jetty.websocket.api.StatusCode;
import java.util.List;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

/** WebSocketSessionManager. */
public abstract class WebSocketSessionManager {
  private static final Logger LOG = LoggerFactory.getLogger(WebSocketSessionManager.class);
  private BiMap<String, Session> sessionMap = HashBiMap.create();

  WebSocketSessionManager(Service sparkService, String endpoint) {
    sparkService.webSocket(ENDPOINT_PREFIX + endpoint, this);
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    String sessionId = getSessionId(session);
    if (sessionId == null) {
      String err =
          String.format(
              "%s not found as a query parameter for websocket session onConnect",
              SecurityServiceImpl.SECURITY_HEADER_KEY);
      session.close(StatusCode.POLICY_VIOLATION, err);
    } else {
      Session existingSession = sessionMap.get(sessionId);

      if (existingSession != null) {
        String reason =
            String.format(
                "Closing old session to switch to new session %s",
                SecurityServiceImpl.SECURITY_HEADER_KEY);
        existingSession.close(StatusCode.NORMAL, reason);
      }

      // Set to no timeout. The SecurityService handles all session timeouts.
      session.setIdleTimeout(0);
      sessionMap.put(sessionId, session);
      LOG.info("Websocket session established: {}", getFormattedWsSessionInfo(session));

      afterOnConnect(session);
    }
  }

  /**
   * Called after the session is connected - override if needed to send an initial message to the
   * client.
   *
   * @param session the websocket session
   */
  protected void afterOnConnect(Session session) {}

  // The subclass must declare @OnWebSocketMessage, since clients
  // may not have different requirements. Some may want binary streamed data, etc.

  BiMap<String, Session> getSessionMap() {
    return sessionMap;
  }

  // Send a text message
  void sendMessage(Session session, Object message) {
    try {
      session.getRemote().sendString(JsonConverter.to(message));
    } catch (Exception e) {
      LOG.error(
          "Cannot send message to websocket session: {}, Message={}",
          getFormattedWsSessionInfo(session),
          message,
          e);
      if (!session.isOpen()) {
        removeSession(session);
      }
    }
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    LOG.info(
        "Session closed: StatusCode={}, Reason={}, {}",
        statusCode,
        reason,
        getFormattedWsSessionInfo(session));
    removeSession(session);

    afterOnClose(session, statusCode, reason);
  }

  protected void afterOnClose(Session session, int statusCode, String reason) {}

  @OnWebSocketError
  public void onError(Session session, Throwable e) {
    LOG.error("Error on session: {}", getFormattedWsSessionInfo(session), e);
    if (!session.isOpen()) {
      removeSession(session);
    }
  }

  private boolean removeSession(Session session) {
    String sessionId = getSessionId(session);
    return sessionId != null && sessionMap.remove(sessionId, session);
  }

  String getSessionId(Session session) {
    List<String> keyValues =
        session.getUpgradeRequest().getParameterMap().get(SecurityServiceImpl.SECURITY_HEADER_KEY);
    if (keyValues != null && keyValues.size() == 1) {
      return keyValues.get(0);
    }

    return null;
  }

  String getFormattedWsSessionInfo(Session session) {
    return String.format(
        "Endpoint=%s, RemoteAddress=%s, ParameterMap=%s",
        session.getUpgradeRequest().getRequestURI().getPath(),
        session.getRemoteAddress(),
        session.getUpgradeRequest().getParameterMap());
  }
}
