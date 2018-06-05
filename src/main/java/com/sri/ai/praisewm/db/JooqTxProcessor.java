package com.sri.ai.praisewm.db;

import com.sri.ai.praisewm.db.internal.JooqConnectionContext;
import java.util.function.Consumer;
import java.util.function.Function;

/** JooqTxProcessor. */
public interface JooqTxProcessor {
  /**
   * Runs a database jooqContext and does not return a result
   *
   * @param jooqContext the jooqContext
   */
  void run(Consumer<JooqContext> jooqContext);

  /**
   * Runs a database jooqContext returning a result.
   *
   * @param jooqContext the jooqContext
   * @param <R> the type of object to return
   * @return the result of the jooqContext
   */
  <R> R query(Function<JooqContext, R> jooqContext);

  /**
   * Get a new connection context.
   *
   * <p>Special purpose method that requires the caller to manage the connection state. {@link #run}
   * or {@link #query} should be used for the vast majority of use cases.
   *
   * @return a new database connection context.
   */
  JooqConnectionContext newConnectionContext();
}
