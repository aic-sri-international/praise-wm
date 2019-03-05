package com.sri.ai.praisewm.web.ws;

import static com.sri.ai.praisewm.web.ws.WebSocketConstants.ENDPOINT_PREFIX;
import static com.sri.ai.praisewm.web.ws.WebsocketUtil.getFormattedWsSessionInfo;

import com.sri.ai.praisewm.util.PropertiesWrapper;
import java.util.List;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

/**
 * The abstract WebSocketSessionManager class interfaces between the Jetty Websocket API and the
 * {@link WsSessionStore}.
 *
 * <p>This class receives Jetty Websocket callbacks for {@code @OnWebSocketConnect},
 * {@code @OnWebSocketClose}, and {@code @OnWebSocketError}.
 */
public abstract class WebSocketSessionManager {
  private static final Logger LOG = LoggerFactory.getLogger(WebSocketSessionManager.class);
  private final WsSessionStore wsSessionStore;

  WebSocketSessionManager(PropertiesWrapper pw, Service sparkService, String endpoint) {
    sparkService.webSocket(ENDPOINT_PREFIX + endpoint, this);
    wsSessionStore = new WsSessionStore(pw);
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    String sessionId = wsSessionStore.onConnect(session);
    if (sessionId != null) {
      afterOnConnect(sessionId);
    }
  }

  List<String> getSessionIds() {
    return wsSessionStore.getSessionIds();
  }

  /**
   * Called after the session is connected - override to perform any post-websocket client
   * connection logic.
   *
   * @param sessionId the assigned websocket session id
   */
  protected void afterOnConnect(String sessionId) {}

  void sendMessage(String sessionId, Object msg) {
    wsSessionStore.sendMessage(sessionId, msg);
  }

  void close(String sessionId, WsStatusCode wsStatusCode, String reason) {
    wsSessionStore.close(sessionId, wsStatusCode, reason);
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    // sessionId may be null
    String sessionId = wsSessionStore.onClose(session, statusCode, reason);
    if (sessionId != null) {
      afterOnClose(sessionId, statusCode, reason);
    }
  }

  /**
   * Called after the session has been closed - override to perform any post-websocket client
   * connection closed logic.
   *
   * @param sessionId the assigned websocket session id
   * @param statusCode the Websocket status code
   * @param reason the reason that the session was closed
   */
  protected void afterOnClose(String sessionId, int statusCode, String reason) {}

  @OnWebSocketError
  public void onError(Session session, Throwable e) {
    // The connection is now suspect, and the connection is likely being shut down
    // So call the close method which will release resources and issue a close call to the
    // underlying
    // session if it is not yet closed. The client should try to restablish the connection
    // if it is able to do so.
    String sessionId = WebsocketUtil.getSessionId(session);
    LOG.error(
        "Error on session: sessionId={}, {}", sessionId, getFormattedWsSessionInfo(session), e);
    wsSessionStore.close(sessionId, WsStatusCode.ON_ERROR, e.getMessage());
  }
}
