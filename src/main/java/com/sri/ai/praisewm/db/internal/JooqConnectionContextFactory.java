package com.sri.ai.praisewm.db.internal;

/**
 * A factory class that provides access to new database connections that are wrapped with access to
 * jOOQ functionality.
 */
public interface JooqConnectionContextFactory {
  /**
   * Get a new {@link JooqConnectionContext}.
   *
   * @return a new jOOQ connection context
   */
  JooqConnectionContext getJooqConnectionContext();
}
