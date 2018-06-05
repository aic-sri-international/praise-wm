package com.sri.ai.praisewm.web.rest.util;

/** RouteScope. */
public final class RouteScope {
  /* Routes that are restricted to the Admin user */
  public static final String ADMIN = "/admin";
  /* Routes that are available to everyone */
  public static final String API = "/api";

  private RouteScope() {}
}
