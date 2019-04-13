package com.sri.ai.praisewm.web.error;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import spark.Request;

/** Exception thrown when there is a problem with a parameter received from a REST call */
public class BadRequestException extends RuntimeException {
  public BadRequestException(Request request, String param, Throwable cause) {
    this(request, param, null, cause);
  }

  public BadRequestException(Request request, String param, String message) {
    this(request, param, message, null);
  }

  public BadRequestException(Request request, String param, String message, Throwable cause) {
    super(toMessage(request, param, message, cause));
  }

  private static String toMessage(Request request, String param, String message, Throwable cause) {
    String msg = trimToEmpty(message);
    if (cause != null) {
      if (!msg.isEmpty()) {
        msg += ": ";
      }
      msg += cause.toString().trim();
    }

    return String.format("Path=%s, Param=%s: %s", request.pathInfo(), param, msg);
  }
}
