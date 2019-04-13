package com.sri.ai.praisewm.db.internal;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.lang3.Validate;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to get a new database connection wrapped within a {@link JooqConnectionContext}.
 */
public class DataSourceFactory implements JooqConnectionContextFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DataSourceFactory.class);
  private DataSource dataSource;
  private SQLDialect sqlDialect;

  void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  void setSqlDialect(String sqlDialect) {
    this.sqlDialect = SQLDialect.valueOf(sqlDialect);
  }

  public void init() {
    Validate.notNull(dataSource, "dataSource cannot be null - verify the configuration files");
    Validate.notNull(sqlDialect, "sqlDialect cannot be null - verify the configuration files");

    LOG.info("DataSource Initialized:\n\t{}\n\tSqlDialect: {}", dataSource, sqlDialect);
  }

  @Override
  public JooqConnectionContext getJooqConnectionContext() {
    Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      throw new DatabaseConnectionException("Cannot get a database connection", e);
    }
    return new DefaultJooqConnectionContext(connection, sqlDialect);
  }
}
