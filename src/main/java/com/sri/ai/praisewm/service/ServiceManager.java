package com.sri.ai.praisewm.service;

import com.google.common.eventbus.EventBus;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import com.sri.ai.praisewm.util.PropertiesWrapper;

/** ServiceManager. */
public interface ServiceManager {
  PropertiesWrapper getConfiguration();

  spark.Service getRestService();

  spark.Service getWebSocketService();

  EventBus getEventBus();

  JooqTxProcessor getJooqTxProcessor();

  <T> T getService(Class<T> service);
}
