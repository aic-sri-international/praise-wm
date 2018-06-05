package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.web.websocket.NotificationSessionManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** NotificationService. */
public class NotificationService implements Service {
  private NotificationSessionManager notifications;
  private Integer pingBroadcastInMillis;
  private ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();

  @Override
  public void start(ServiceManager serviceManager) {
    notifications =
        new NotificationSessionManager(
            serviceManager.getWebSocketService(),
            serviceManager.getEventBus(),
            serviceManager.getService(SecurityServiceImpl.class));

    pingBroadcastInMillis = serviceManager.getConfiguration().asInt("server.ws.pingClientInMillis");

    pingScheduler.scheduleWithFixedDelay(
        () -> notifications.broadcastEvent(new PingEvent()),
        pingBroadcastInMillis,
        pingBroadcastInMillis,
        TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    pingScheduler.shutdownNow();

    if (notifications != null) {
      notifications.broadcastEvent(new SystemShutdownEvent());
    }
  }

  private static class PingEvent extends NotificationEvent {
    @Override
    public String toString() {
      return "Ping{} " + super.toString();
    }
  }

  private static class SystemShutdownEvent extends NotificationEvent {
    @Override
    public String toString() {
      return "SystemShutdownEvent{} " + super.toString();
    }
  }
}
