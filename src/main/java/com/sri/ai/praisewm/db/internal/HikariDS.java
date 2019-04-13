package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.util.PropertiesWrapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class starts and shuts down the HikariCP database connection pool */
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
    String host = pw.asString("database.host");
    String port = pw.asString("database.port");

    String envHost = pw.asString("database.host.env");
    String altHost = System.getenv(envHost);
    if (altHost != null) {
      host = altHost;
    }

    url = replaceUrlToken(url, "_HOST_", host);
    url = replaceUrlToken(url, "_PORT_", port);

    LOG.info("Database URL: '{}'", url);

    return url;
  }

  private String replaceUrlToken(String url, String token, String replacement) {
    String urlActual = url.replace(token, replacement);
    if (urlActual.equals(url)) {
      throw new IllegalArgumentException(
          String.format("database.url property does not contain expected token: %s", token));
    }
    return urlActual;
  }

  HikariDataSource getDataSource() {
    return dataSource;
  }

  void shutdown() {
    dataSource.close();
  }
}
