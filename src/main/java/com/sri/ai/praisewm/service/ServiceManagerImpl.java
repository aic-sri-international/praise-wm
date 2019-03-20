package com.sri.ai.praisewm.service;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.db.internal.DatabaseConnectionManager;
import com.sri.ai.praisewm.db.internal.JooqTxProcessorImpl;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.web.WebController;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.Validate;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ServiceManagerImpl. */
public class ServiceManagerImpl implements ServiceManager {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceManagerImpl.class);
  private final LinkedHashMap<Class, Service> services = new LinkedHashMap<>();
  private final EventBus eventBus;
  private final Callable<WebController> webControllerInitializer;
  private final spark.Service sparkService;
  private DatabaseConnectionManager dbManager;
  private PropertiesWrapper configuration;
  private Flyway flyway;

  public ServiceManagerImpl(
      PropertiesWrapper propertiesWrapper,
      EventBus eventBus,
      Callable<WebController> webControllerInitializer,
      spark.Service sparkService) {
    this.eventBus = eventBus;
    this.webControllerInitializer = webControllerInitializer;
    this.sparkService = sparkService;
    configuration = propertiesWrapper;
    dbManager = new DatabaseConnectionManager(propertiesWrapper);
    dbManager.start(
        (dataSource) -> {
          flyway = Flyway.configure().dataSource(dataSource).load();
          flyway.migrate();
        });
  }

  @Override
  public PropertiesWrapper getConfiguration() {
    return configuration;
  }

  @Override
  public spark.Service getSparkService() {
    return sparkService;
  }

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public JooqTxProcessor getJooqTxProcessor() {
    return new JooqTxProcessorImpl(dbManager.getJooqCtxFactory());
  }

  public void addService(Class<? extends Service> serviceClass) {
    Service service;
    try {
      service = serviceClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Cannot create an instance of service %s", serviceClass.getName()), e);
    }

    addService(service);
  }

  private void addService(Service service) {
    services.put(service.getClass(), service);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> service) {
    return (T) Validate.notNull(services.get(service));
  }

  public void startServices() {
    LOG.info("Starting {} Services...", services.size());

    services
        .values()
        .forEach(
            s -> {
              try {
                s.start(this);
                LOG.info("{} Started", s.getClass().getName());
                if (s instanceof NotificationService) {
                  // WebController initialization must occur after the creation of any Spark
                  // Websocket handlers and before initializing any services that create
                  // REST routes.
                  webControllerInitializer.call();
                }
              } catch (Exception e) {
                throw new RuntimeException("Cannot start service: " + s.getClass().getName(), e);
              }
            });
    LOG.info("{} Services Started", services.size());
  }

  public void stopServices() {
    LOG.info("Stopping Services...");
    services
        .values()
        .forEach(
            s -> {
              try {
                s.stop();
                LOG.info("{} Stopped", s.getClass().getName());
              } catch (Exception e) {
                LOG.error("Cannot stop service {}", s.getClass().getName(), e);
              }
            });

    LOG.info("Stopping Database Manager...");
    dbManager.stop();
    LOG.info("Database Manager Stopped");
  }
}
