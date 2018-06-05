package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.web.rest.route.NotificationInputRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;

/**
 * NotificationInputService provides the means for a client to send a notification event to other
 * clients.
 */
public class NotificationInputService implements Service {

  @Override
  public void start(ServiceManager serviceManager) {
    new NotificationInputRoutes(
        serviceManager.getRestService(), RouteScope.API, serviceManager.getEventBus());
  }
}
