package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.util.PropertiesWrapper;
import java.util.function.Consumer;
import javax.sql.DataSource;

/** DatabaseConnectionManager. */
public class DatabaseConnectionManager {
  private final PropertiesWrapper pw;
  private HikariDS connectionPool;
  private DataSourceFactory dataSourceFactory;

  public DatabaseConnectionManager(PropertiesWrapper propertiesWrapper) {
    this.pw = propertiesWrapper;
  }

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

  public JooqConnectionContextFactory getJooqCtxFactory() {
    return dataSourceFactory;
  }

  public void stop() {
    connectionPool.shutdown();
  }
}
