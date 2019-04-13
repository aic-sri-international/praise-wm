package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.util.PropertiesWrapper;
import java.util.function.Consumer;
import javax.sql.DataSource;

/**
 * Manages database connections.
 *
 * <p>Starts and stops the database connection pool and provides access to get a new database
 * connection.
 */
public class DatabaseConnectionManager {
  private final PropertiesWrapper pw;
  private HikariDS connectionPool;
  private DataSourceFactory dataSourceFactory;

  public DatabaseConnectionManager(PropertiesWrapper propertiesWrapper) {
    this.pw = propertiesWrapper;
  }

  /**
   * Starts the database connection pool.
   *
   * @param migrationProcessor optional callback to start a database migration service, such as
   *     Flyway
   */
  public void start(Consumer<DataSource> migrationProcessor) {
    connectionPool = new HikariDS(pw);
    connectionPool.startup();

    if (migrationProcessor != null) {
      migrationProcessor.accept(connectionPool.getDataSource());
    }

    dataSourceFactory = new DataSourceFactory();
    dataSourceFactory.setDataSource(connectionPool.getDataSource());
    dataSourceFactory.setSqlDialect(pw.asString("database.jooq.sql.dialect"));
    dataSourceFactory.init();
  }

  /**
   * Get a factory class that can provide new database connections.
   *
   * @return the connection factory class
   */
  public JooqConnectionContextFactory getJooqCtxFactory() {
    return dataSourceFactory;
  }

  /** Stops the database connection pool. */
  public void stop() {
    connectionPool.shutdown();
  }
}
