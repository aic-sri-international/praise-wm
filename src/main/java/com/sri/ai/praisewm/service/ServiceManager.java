package com.sri.ai.praisewm.service;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.util.PropertiesWrapper;

/**
 * The ServiceManager starts and stops all {@link com.sri.ai.praisewm.service services} and provides
 * mechanisms for them to communicate between one another and higher level services.
 */
public interface ServiceManager {
  /**
   * Get global configuration data required by services.
   *
   * @return the configuration access object.
   */
  PropertiesWrapper getConfiguration();

  /**
   * Get the native Spark Java Service class used to establish REST routes.
   *
   * @return the native REST Spark Java Service class
   */
  spark.Service getSparkService();

  /**
   * Get the global EventBus for inter-service communications.
   *
   * @return the event bus
   */
  EventBus getEventBus();

  /**
   * Get the jOOQ transaction processor.
   *
   * <p>Use for all database access.
   *
   * @return the transaction processor
   */
  JooqTxProcessor getJooqTxProcessor();

  /**
   * Get a reference to another service class.
   *
   * <p>Since using this method more tightly couples service classes, it should only be used when
   * the {@link #getEventBus event bus} is not sufficient.
   *
   * @param service the name of the service class to access
   * @param <T> The type of service class to access
   * @return the service class instance
   * @throws RuntimeException if the service is not in the {@link
   *     com.sri.ai.praisewm.service.ServiceRegistry}
   */
  <T> T getService(Class<T> service);
}
