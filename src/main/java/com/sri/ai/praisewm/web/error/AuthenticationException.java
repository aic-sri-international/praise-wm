package com.sri.ai.praisewm.web.error;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import spark.Request;

/**
 * Exception thrown when a REST request is received but either the header does not contain a
 * sessionId, or, the sessionId does not match that of an existing session.
 */
public class AuthenticationException extends RuntimeException {
  public AuthenticationException(Request request, String message) {
    super(toMessage(request, message));
  }

  private static String toMessage(Request request, String message) {
    String msg = trimToEmpty(message);
    return String.format("%s Path=%s", request.pathInfo(), msg);
  }
}
