package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.web.rest.route.AdminRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;

/** AdminService. */
public class AdminService implements Service {
  @Override
  public void start(ServiceManager serviceManager) {
    new AdminRoutes(
        serviceManager.getRestService(),
        RouteScope.ADMIN,
        serviceManager.getJooqTxProcessor(),
        serviceManager.getEventBus());
  }
}
