package com.sri.ai.praisewm.web.websocket;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.event.notification.SessionCloseEvent;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.Service;

/** EchoSessionManager */
@WebSocket
public class EchoSessionManager extends WebSocketSessionManager {
  private static final String ENDPOINT_SUFFIX = "echo";

  public EchoSessionManager(Service sparkService, EventBus eventBus) {
    super(sparkService, ENDPOINT_SUFFIX);
    eventBus.register(new SessionCloseEventListener());
  }

  protected void afterOnConnect(Session session) {
    sendMessage(session, "Hello there ! I'll echo all of your messages.");
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    sendMessage(session, message);
  }

  private class SessionCloseEventListener {
    @Subscribe
    public void closeSession(SessionCloseEvent closeEvent) {
      Session session = getSessionMap().remove(closeEvent.getSessionId());
      if (session != null) {
        session.close(StatusCode.NORMAL, closeEvent.getText());
      }
    }
  }
}
