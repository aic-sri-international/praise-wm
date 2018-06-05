package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.db.ConnectionContext;
import com.sri.ai.praisewm.db.JooqContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DefaultJooqConnectionContext */
public class DefaultJooqConnectionContext implements JooqConnectionContext {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultJooqConnectionContext.class);
  private DefaultConfiguration configuration;
  private DefaultConnectionProvider provider;
  private DSLContext dslContext;

  DefaultJooqConnectionContext(Connection connection, SQLDialect sqlDialect) {
    provider = new DefaultConnectionProvider(connection);
    configuration = new DefaultConfiguration();
    configuration.set(sqlDialect);
    configuration.set(provider);
    dslContext = DSL.using(configuration);
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public DSLContext getDslContext() {
    return dslContext;
  }

  public JooqContext getJooqContext() {
    return new JooqContext() {

      @Override
      public ConnectionContext connectionContext() {
        return DefaultJooqConnectionContext.this;
      }

      @Override
      public Configuration configuration() {
        return configuration;
      }

      @Override
      public DSLContext dslContext() {
        return dslContext;
      }
    };
  }

  @Override
  public void commit() throws DataAccessException {
    provider.commit();
  }

  @Override
  public void close() {
    if (provider != null) {
      Connection connection = provider.acquire();
      try {
        if (connection != null && !connection.isClosed()) {
          connection.close();
        }
      } catch (SQLException e) {
        LOG.error("Error closing DB connection", e);
      }
    }
  }

  @Override
  public Connection acquire() {
    return provider.acquire();
  }

  @Override
  public boolean getAutoCommit() throws DataAccessException {
    return provider.getAutoCommit();
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws DataAccessException {
    provider.setAutoCommit(autoCommit);
  }

  @Override
  public void rollback(Savepoint savepoint) throws DataAccessException {
    provider.rollback(savepoint);
  }

  @Override
  public int getTransactionIsolation() throws DataAccessException {
    return provider.getTransactionIsolation();
  }

  @Override
  public void setTransactionIsolation(int level) throws DataAccessException {
    provider.setTransactionIsolation(level);
  }

  @Override
  public Savepoint setSavepoint(String name) throws DataAccessException {
    return provider.setSavepoint(name);
  }

  @Override
  public Savepoint setSavepoint() throws DataAccessException {
    return provider.setSavepoint();
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws DataAccessException {
    provider.releaseSavepoint(savepoint);
  }

  @Override
  public void rollback() throws DataAccessException {
    provider.rollback();
  }

  @Override
  public int getHoldability() throws DataAccessException {
    return provider.getHoldability();
  }

  @Override
  public void setHoldability(int holdability) throws DataAccessException {
    provider.setHoldability(holdability);
  }
}
