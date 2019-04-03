package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.web.ws.EchoSessionManager;

/** Simple Websocket service for example purposes */
public class EchoService implements Service {
  private EchoSessionManager echoSessionManager;

  @Override
  public void start(ServiceManager serviceManager) {
    echoSessionManager =
        new EchoSessionManager(
            serviceManager.getConfiguration(),
            serviceManager.getSparkService(),
            serviceManager.getEventBus());
  }
}
