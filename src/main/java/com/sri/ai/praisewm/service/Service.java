package com.sri.ai.praisewm.service;

/** Service. */
public interface Service {
  void start(ServiceManager serviceManager);

  default void stop() {}
}
