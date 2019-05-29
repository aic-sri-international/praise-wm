package com.sri.ai.praisewm.web.ws;

import static com.sri.ai.praisewm.web.ws.WebsocketUtil.getFormattedWsSessionInfo;

import com.sri.ai.praisewm.event.notification.DataRefreshEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.util.JsonConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The WsSessionWrapper processes WebSocket messages being sent to a single client session.
 *
 * <p>So that the client has time to process {@link DataRefreshEvent} messages, identical {@link
 * DataRefreshEvent} messages will not be sent within a specified refresh interval while
 * guaranteeing that the last one is sent.
 */
public class WsSessionWrapper {
  private static final Logger LOG = LoggerFactory.getLogger(WsSessionWrapper.class);
  private final Object EVENT_LOCK = new Object();
  private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private Runnable messageDispatch;
  private List<DataRefreshEvent> events = new ArrayList<>();
  private Session session;
  private String sessionId;
  private boolean error;
  private boolean closeCalled;
  private long interval;

  WsSessionWrapper(Session session, String sessionId, long interval) {
    this.session = session;
    this.sessionId = sessionId;
    this.interval = interval;

    // Set to no timeout. The SecurityService handles all session timeouts.
    this.session.setIdleTimeout(0);
  }

  public boolean isError() {
    return error;
  }

  public void close(WsStatusCode statusCode, String reason) {
    if (closeCalled) {
      return;
    }
    closeCalled = true;
    stop();
    if (session.isOpen()) {
      session.close(statusCode.code(), reason);
    }
  }

  public String getSessionId() {
    return sessionId;
  }

  void stop() {
    events.clear();
    if (!executor.isShutdown()) {
      executor.shutdownNow();
    }
  }

  private void setMessageTimer() {
    messageDispatch =
        () -> {
          synchronized (EVENT_LOCK) {
            if (events.isEmpty()) {
              messageDispatch = null;
            } else {
              NotificationEvent event = events.get(0);
              events.remove(0);
              sendMessage_(event);
              setMessageTimer();
            }
          }
        };
    if (!executor.isShutdown()) {
      executor.schedule(messageDispatch, interval, TimeUnit.MILLISECONDS);
    }
  }

  private void sendOrEnqueueEvent(DataRefreshEvent event) {
    synchronized (EVENT_LOCK) {
      if (events.isEmpty()) {
        if (messageDispatch == null) {
          sendMessage_(event);
          setMessageTimer();
        } else {
          events.add(event);
        }
      } else {
        boolean found = events.stream().anyMatch(p -> p.getRefreshType() == event.getRefreshType());
        if (!found) {
          events.add(event);
        }
      }
    }
  }

  /**
   * Send a message to the websocket
   *
   * @param message message to be sent - will be converted to JSON
   */
  void sendMessage(Object message) {
    if (error) {
      return;
    }

    if (interval > 0 && message instanceof DataRefreshEvent) {
      sendOrEnqueueEvent((DataRefreshEvent) message);
    } else {
      sendMessage_(message);
    }
  }

  private synchronized void sendMessage_(Object message) {
    if (error) {
      return;
    }

    try {
      session.getRemote().sendString(JsonConverter.to(message));
    } catch (Exception e) {
      LOG.error(
          "Cannot send message to websocket session: {}, Message={}",
          getFormattedWsSessionInfo(session),
          message,
          e);
      error = true;
      close(WsStatusCode.ON_ERROR, "Closing due to error on sendString");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WsSessionWrapper that = (WsSessionWrapper) o;
    return Objects.equals(session, that.session);
  }

  @Override
  public int hashCode() {
    return Objects.hash(session);
  }
}
