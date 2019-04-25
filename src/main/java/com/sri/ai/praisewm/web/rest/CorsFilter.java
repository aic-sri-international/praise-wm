package com.sri.ai.praisewm.web.rest;

import java.util.LinkedHashMap;
import java.util.Map;
import spark.Request;
import spark.Response;

/** CorsFilter provides CORS for JavaSpark */
public final class CorsFilter {
  private static final Map<String, String> corsHeaders = new LinkedHashMap<>();

  static {
    corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
    corsHeaders.put("Access-Control-Allow-Origin", "*");
    corsHeaders.put(
        "Access-Control-Allow-Headers",
        "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
    corsHeaders.put("Access-Control-Allow-Credentials", "true");
  }

  private spark.Service sparkService;

  public CorsFilter(spark.Service sparkService) {
    this.sparkService = sparkService;
  }

  public void apply() {
    sparkService.after((Request req, Response res) -> corsHeaders.forEach(res::header));
  }

  public void apply(Response res) {
    corsHeaders.forEach(res::header);
  }
}
