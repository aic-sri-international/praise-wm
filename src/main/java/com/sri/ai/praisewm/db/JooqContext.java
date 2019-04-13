package com.sri.ai.praisewm.db;

import org.jooq.Configuration;
import org.jooq.DSLContext;

/** Defines the jOOQ context. */
public interface JooqContext {
  /**
   * Get the context used to access the jOOQ DSL.
   *
   * @return the jOOQ DSL context
   */
  DSLContext dslContext();

  /**
   * Get the jOOQ Configuration object.
   *
   * @return the jOOQ Configuration
   */
  Configuration configuration();

  /**
   * Get access to internal JDBC connection details.
   *
   * <p>This method is usually only needed by classes internal to the framework.
   *
   * @return the connection context
   */
  ConnectionContext connectionContext();
}
