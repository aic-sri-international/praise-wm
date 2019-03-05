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
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  private final EventBus eventBus = new EventBus("ApplicationEventBus");
  private PropertiesWrapper pw;
  private ServiceRegistry serviceRegistry;
  private ServiceManagerImpl serviceManager;
  private SecurityService securityService;
  private WebController webController;
  private DeadEventListener deadEventListener = new DeadEventListener();

  public Application() {
    try {
      pw = PropertiesWrapper.fromFile(Paths.get("application.properties"));
      LOG.info("\nApplication Properties:\n{}", pw.format());
      serviceRegistry = new ServiceRegistry(pw);
      start();
    } catch (Exception e) {
      LOG.error("Application initialization error", e);
      System.exit(1);
    }
  }

  private void start() {
    ShutdownThread shutdownHookThread = new ShutdownThread(this::stop);
    Runtime.getRuntime().addShutdownHook(shutdownHookThread);

    eventBus.register(deadEventListener);

    webController = new WebController(pw, () -> securityService, 0);

    serviceManager =
        new ServiceManagerImpl(
            pw, eventBus, () -> webController.init(), webController.getSparkService());

    serviceRegistry.get().forEach(serviceManager::addService);

    serviceManager.startServices();
    securityService = serviceManager.getService(SecurityServiceImpl.class);
  }

  private void stop() {
    serviceManager.stopServices();
    webController.stop();
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
