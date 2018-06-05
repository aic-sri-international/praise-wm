package com.sri.ai.praisewm.db;

import java.sql.Connection;
import java.sql.Savepoint;
import org.jooq.exception.DataAccessException;

/** ConnectionContext. */
public interface ConnectionContext {
  void commit() throws DataAccessException;

  int getTransactionIsolation() throws DataAccessException;

  void setTransactionIsolation(int level) throws DataAccessException;

  Savepoint setSavepoint(String name) throws DataAccessException;

  Savepoint setSavepoint() throws DataAccessException;

  void releaseSavepoint(Savepoint savepoint) throws DataAccessException;

  void rollback(Savepoint savepoint) throws DataAccessException;

  void rollback() throws DataAccessException;

  int getHoldability() throws DataAccessException;

  void setHoldability(int holdability) throws DataAccessException;

  Connection acquire();
}
