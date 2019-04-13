package com.sri.ai.praisewm.web.error;

/** Exception thrown when a user is not authorized to access a specific REST method. */
public class AuthorizationException extends RuntimeException {
  public AuthorizationException(String message) {
    super(message);
  }
}
