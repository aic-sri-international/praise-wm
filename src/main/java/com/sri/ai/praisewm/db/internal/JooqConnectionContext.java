package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.db.ConnectionContext;
import com.sri.ai.praisewm.db.JooqContext;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

/** JooqConnectionContext */
public interface JooqConnectionContext extends AutoCloseable, ConnectionContext {
  DSLContext getDslContext();

  Configuration getConfiguration();

  JooqContext getJooqContext();

  void close();

  boolean getAutoCommit() throws DataAccessException;

  void setAutoCommit(boolean autoCommit) throws DataAccessException;
}
