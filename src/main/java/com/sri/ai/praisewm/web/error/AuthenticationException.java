package com.sri.ai.praisewm.web.error;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import spark.Request;

/** AuthenticationException. */
public class AuthenticationException extends RuntimeException {
  public AuthenticationException(Request request, String message) {
    super(toMessage(request, message));
  }

  private static String toMessage(Request request, String message) {
    String msg = trimToEmpty(message);
    return String.format("%s Path=%s", request.pathInfo(), msg);
  }
}
