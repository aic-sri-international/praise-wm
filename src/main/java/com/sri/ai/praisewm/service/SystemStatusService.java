package com.sri.ai.praisewm.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.db.internal.JooqConnectionContext;
import com.sri.ai.praisewm.event.SessionStartedEvent;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Broadcast;
import com.sri.ai.praisewm.event.notification.NotificationEvent.Level;
import com.sri.ai.praisewm.event.notification.SystemStatusEvent;
import com.sri.ai.praisewm.event.notification.SystemStatusEvent.SystemStatusType;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SystemStatusService is the main service to process and handle functions related to the system
 * status.
 */
public class SystemStatusService implements Service {
  private static final Logger LOG = LoggerFactory.getLogger(SystemStatusService.class);
  private static final int DB_HEALTH_CHECK_INTERVAL_IN_MILLIS = 1000;
  private EventBus eventBus;
  private Map<SystemStatusType, Level> systemStatuses = new HashMap<>();
  private SessionOpenedEventListener sessionOpenedEventListener;
  private ScheduledExecutorService healthCheckService = Executors.newScheduledThreadPool(2);
  private JooqTxProcessor txp;
  private boolean shutdown = false;

  @Override
  public void start(ServiceManager serviceManager) {
    txp = serviceManager.getJooqTxProcessor();

    systemStatuses.put(SystemStatusType.DATABASE, Level.INFO);
    eventBus = serviceManager.getEventBus();

    sessionOpenedEventListener = new SessionOpenedEventListener();
    eventBus.register(sessionOpenedEventListener);

    healthCheckService.schedule(new DatabaseHealthChecker(), 500, MILLISECONDS);
  }

  @Override
  public void stop() {
    shutdown = true;

    if (eventBus != null) {
      if (sessionOpenedEventListener != null) {
        eventBus.unregister(sessionOpenedEventListener);
      }
    }

    healthCheckService.shutdownNow();
  }

  private void setLevel(SystemStatusType type, Level level) {
    if (shutdown) {
      return;
    }
    Level priorLevel = systemStatuses.put(type, level);
    if (level != priorLevel) {
      SystemStatusEvent sse = new SystemStatusEvent();
      sse.setSessionId("SystemStatusService");
      sse.setBroadcast(Broadcast.EXCLUSIVE);
      sse.setLevel(level);
      sse.setSystemStatuses(Collections.singletonMap(type, level));
      eventBus.post(sse);
    }
  }

  class DatabaseHealthChecker implements Runnable {
    @Override
    public void run() {
      try {
        healthChecker();
      } catch (Exception ex) {
        LOG.error("DatabaseHealthChecker abnormal termination", ex);
      }
    }

    private void healthChecker() {
      while (!shutdown) {
        checkDatabase();
        setDbLevel(Level.ERROR);
        try {
          Thread.sleep(DB_HEALTH_CHECK_INTERVAL_IN_MILLIS);
        } catch (InterruptedException e) {
          LOG.info("DatabaseHealthChecker termination on interrupt");
          return;
        }
      }
    }

    private void checkDatabase() {
      Connection conn = getNewConnection();
      if (conn != null) {
        try {
          while (conn.isValid(1)) {
            Thread.sleep(DB_HEALTH_CHECK_INTERVAL_IN_MILLIS);
          }
        } catch (Exception ex) {
          //
        }
      }
    }

    private Connection getNewConnection() {
      Connection conn;
      try {
        JooqConnectionContext jcc = txp.newConnectionContext();
        conn = jcc.acquire();
        if (conn.isValid(1)) {
          setDbLevel(Level.INFO);
        }
      } catch (Exception ex) {
        conn = null;
      }
      return conn;
    }

    private void setDbLevel(Level level) {
      setLevel(SystemStatusType.DATABASE, level);
    }
  }

  private class SessionOpenedEventListener {
    @Subscribe
    public void sessionStarted(SessionStartedEvent event) {
      SystemStatusEvent sse = new SystemStatusEvent();
      sse.setSessionId(event.getSessionId());
      sse.setLevel(Level.INFO);
      sse.setSystemStatuses(systemStatuses);
      eventBus.post(sse);
    }
  }
}
