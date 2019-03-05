package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.web.rest.route.UserRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;
import org.apache.commons.lang3.Validate;

/** UserService. */
public class UserService implements Service {
  @Override
  public void start(ServiceManager serviceManager) {
    SecurityService securityService =
        Validate.notNull(
            serviceManager.getService(SecurityServiceImpl.class),
            "%s has not been loaded",
            SecurityServiceImpl.class.getName());

    new UserRoutes(
        serviceManager.getSparkService(),
        RouteScope.API,
        securityService,
        serviceManager.getJooqTxProcessor());
  }
}
