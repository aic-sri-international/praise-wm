package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** HikariDS */
class HikariDS {

  private static final Logger LOG = LoggerFactory.getLogger(HikariDS.class);

  private HikariDataSource dataSource;
  private PropertiesWrapper pw;

  HikariDS(PropertiesWrapper propertiesWrapper) {
    this.pw = propertiesWrapper;
  }

  void startup() {
    final String jdbcUrl = getJdbcUrl();
    MySqlDataSourceProperties dsProps = new MySqlDataSourceProperties();
    dsProps.setUser(pw.asString("database.user"));
    dsProps.setPassword(pw.asString("database.password"));
    dsProps.setJdbcUrl(jdbcUrl);
    dsProps.setCachePrepStmts(true);
    dsProps.setPrepStmtCacheSize(100);
    dsProps.setPrepStmtCacheSqlLimit(2048);

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setDataSourceProperties(dsProps);
    config.setMaximumPoolSize(pw.asInt("database.pool.size"));
    config.setAutoCommit(false);
    config.setPoolName(pw.asString("database.schema"));

    dataSource = new HikariDataSource(config);
  }

  private String getJdbcUrl() {
    String url = pw.asString("database.url");

    String envHost = pw.asString("database.host.env");
    String host = System.getenv(envHost);
    if (host != null) {
      int start = url.indexOf("//");
      int end = url.indexOf(':', start);
      url = url.substring(0, start + 2) + host + url.substring(end);
      LOG.info("Database URL: '{}'", url);
    }
    return url;
  }

  HikariDataSource getDataSource() {
    return dataSource;
  }

  void shutdown() {
    dataSource.close();
  }
}
