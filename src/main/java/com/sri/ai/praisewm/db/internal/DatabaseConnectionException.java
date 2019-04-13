package com.sri.ai.praisewm.db.internal;

/** Exception thrown if there is a problem getting a new database connection. */
public class DatabaseConnectionException extends RuntimeException {

  DatabaseConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
