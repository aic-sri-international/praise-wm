package com.sri.ai.praisewm;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sri.ai.praisewm.service.SecurityService;
import com.sri.ai.praisewm.service.SecurityServiceImpl;
import com.sri.ai.praisewm.service.ServiceManagerImpl;
import com.sri.ai.praisewm.service.ServiceRegistry;
import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.sri.ai.praisewm.web.WebController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  private final EventBus eventBus = new EventBus("ApplicationEventBus");
  private ServiceManagerImpl serviceManager;
  private SecurityService securityService;
  private WebController webResource;
  // WebSocket session endpoints are created using a separate spark.Service instance
  // to work-around this Spark websocket bug: https://github.com/perwendel/spark/issues/783
  private WebController websocketResource;
  private DeadEventListener deadEventListener = new DeadEventListener();

  public static void main(String[] args) {
    try {
      Application application = new Application();
      application.start();
    } catch (Exception e) {
      LOG.error("Application initialization error", e);
      System.exit(1);
    }
  }

  private void start() {
    PropertiesWrapper pw = PropertiesWrapper.fromClasspath("com.sri.ai.praisewm.cfg");
    LOG.info("\nApplication Properties:\n{}", pw.format());

    ShutdownThread shutdownHookThread = new ShutdownThread(this::stop);
    Runtime.getRuntime().addShutdownHook(shutdownHookThread);

    eventBus.register(deadEventListener);

    webResource = new WebController(pw, () -> securityService, 0);

    websocketResource = new WebController(pw, () -> securityService, 1);

    serviceManager =
        new ServiceManagerImpl(
            pw, eventBus, webResource.getSparkService(), websocketResource.getSparkService());

    ServiceRegistry.get().forEach(serviceManager::addService);
    // We need to complete the webResouce initialization before we start the services
    // that will establish the REST endpoints.
    webResource.init();

    serviceManager.startServices();
    securityService = serviceManager.getService(SecurityServiceImpl.class);

    // All websocket endpoints must be created prior to initializing the web resource used for
    // websockets.
    //    EchoSessionManager echoSessionManager =
    //        new EchoSessionManager(websocketResource.getSparkService(), eventBus);

    websocketResource.init();
  }

  private void stop() {
    serviceManager.stopServices();
    websocketResource.stop();
    webResource.stop();
  }

  public static class DeadEventListener {
    @Subscribe
    public void log(DeadEvent deadEvent) {
      LOG.warn("Event sent without any listeners: {}", deadEvent);
    }
  }

  public static class ShutdownThread extends Thread {
    private ShutdownThread systemShutdownHook;
    private int waitTime;

    private ShutdownThread(Runnable target) {
      super(target);
      this.setName("Application Shutdown Thread");
    }

    private ShutdownThread(Runnable target, int waitTime, ShutdownThread systemShutdownHook) {
      this(target);
      this.waitTime = waitTime;
      this.systemShutdownHook = systemShutdownHook;
    }

    public void run() {
      LOG.info("Application Shutting down in {} seconds", waitTime);

      if (systemShutdownHook != null) {
        Runtime.getRuntime().removeShutdownHook(systemShutdownHook);
      }

      pause(waitTime * 1000);

      super.run();

      LOG.info("{} Completed", getName());
    }

    private void pause(int millis) {
      try {
        sleep(millis);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
}
