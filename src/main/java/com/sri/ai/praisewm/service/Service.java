package com.sri.ai.praisewm.service;

/** All services must implement this interface */
public interface Service {
  /**
   * The service's initialization method.
   *
   * <p>This method is called by the {@link ServiceManager} when the application is starting.
   *
   * <p>The service should perform any required initialization from this method.
   *
   * <p>The ServiceManager will not start any other services until this method returns.
   *
   * @param serviceManager the service manager
   */
  void start(ServiceManager serviceManager);

  /**
   * The service's stop method.
   *
   * <p>This method is called by the {@link ServiceManager} when the application is shutting down.
   *
   * <p>The service should immediately close any open resources and stop any threads it has started.
   * The service manager will not stop other services until this method returns, so, the service
   * should not do any unnecessary processing.
   */
  default void stop() {}
}
