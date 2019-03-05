package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.event.notification.NotificationEvent;
import com.sri.ai.praisewm.web.ws.NotificationSessionManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Notification Service sends events to UI clients using the {@link NotificationSessionManager}.
 *
 * <p>A {@code PingEvent} is broadcast to all connected clients every {@code
 * server.ws.pingClientInMillis} which is configured in the application's {@code com.jpanther.cfg}
 * file.
 *
 * <p>When this service's {@link #stop} method is called, it will broadcast a {@link
 * SystemShutdownEvent} to all connected clients.
 */
public class NotificationService implements Service {
  private NotificationSessionManager notifications;
  private Integer pingBroadcastInMillis;
  private ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();

  @Override
  public void start(ServiceManager serviceManager) {
    notifications =
        new NotificationSessionManager(
            serviceManager.getConfiguration(),
            serviceManager.getSparkService(),
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
