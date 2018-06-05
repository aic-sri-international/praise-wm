package com.sri.ai.praisewm.web.rest.route;

import spark.Route;
import spark.RouteGroup;

/** AbstractRouteGroup. */
public abstract class AbstractRouteGroup implements RouteGroup {
  private spark.Service sparkService;

  public AbstractRouteGroup(spark.Service sparkService, String scope) {
    this.sparkService = sparkService;
    this.sparkService.path(scope, this);
  }

  public void get(String path, Route route) {
    sparkService.get(path, route);
  }

  public void post(String path, Route route) {
    sparkService.post(path, route);
  }

  public void put(String path, Route route) {
    sparkService.put(path, route);
  }

  public void delete(String path, Route route) {
    sparkService.delete(path, route);
  }
}
