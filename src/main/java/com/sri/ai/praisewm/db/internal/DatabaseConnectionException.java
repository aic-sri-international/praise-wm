package com.sri.ai.praisewm.db.internal;

/** DatabaseConnectionException. */
public class DatabaseConnectionException extends RuntimeException {

  DatabaseConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
