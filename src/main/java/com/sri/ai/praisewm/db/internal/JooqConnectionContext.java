package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.db.ConnectionContext;
import com.sri.ai.praisewm.db.JooqContext;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

/** Defines a single database connection with access to jOOQ functionality. */
public interface JooqConnectionContext extends AutoCloseable, ConnectionContext {
  /**
   * Get the context to use the jOOQ DSL.
   *
   * @return the jOOQ DSL context
   */
  DSLContext getDslContext();

  /**
   * Get the jOOQ {@code Configuration} object.
   *
   * @return the jOOQ configuration object
   */
  Configuration getConfiguration();

  /**
   * Get the context used by the {@link com.sri.ai.praisewm.db.JooqTxProcessor}
   *
   * @return the jOOQ context
   */
  JooqContext getJooqContext();

  /** Closes the database connection. */
  void close();

  /**
   * Determine if the connection is using auto commit.
   *
   * <p>A jOOQ DataAccessException is thrown if an error occurs
   *
   * @return true if using auto commit
   */
  boolean getAutoCommit() throws DataAccessException;

  /**
   * Set auto commit for the connection.
   *
   * <p>A jOOQ DataAccessException is thrown if an error occurs
   *
   * @param autoCommit true to turn on auto commit
   */
  void setAutoCommit(boolean autoCommit) throws DataAccessException;
}
