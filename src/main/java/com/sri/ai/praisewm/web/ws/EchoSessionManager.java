package com.sri.ai.praisewm.web.ws;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.event.notification.SessionCloseEvent;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.Service;

/** EchoSessionManager simple example of a websocket service */
@WebSocket
public class EchoSessionManager extends WebSocketSessionManager {
  private static final String ENDPOINT_SUFFIX = "echo";

  public EchoSessionManager(PropertiesWrapper pw, Service sparkService, EventBus eventBus) {
    super(pw, sparkService, ENDPOINT_SUFFIX);
    eventBus.register(new SessionCloseEventListener());
  }

  protected void afterOnConnect(String sessionId) {
    sendMessage(sessionId, "Hello there ! I'll echo all of your messages.");
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    sendMessage(WebsocketUtil.getSessionId(session), message);
  }

  private class SessionCloseEventListener {
    @Subscribe
    public void closeSession(SessionCloseEvent closeEvent) {
      close(closeEvent.getSessionId(), WsStatusCode.NORMAL, closeEvent.getText());
    }
  }
}
