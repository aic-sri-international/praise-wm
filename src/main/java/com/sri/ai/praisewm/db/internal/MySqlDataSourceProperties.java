package com.sri.ai.praisewm.db.internal;

import java.util.Properties;
import org.apache.commons.lang3.Validate;

/** Utility class to set properties used by a MySQL DataSource */
class MySqlDataSourceProperties extends Properties {
  public void setUser(String user) {
    Validate.notBlank(user, "user cannot be null");
    put("user", user.trim());
  }

  public void setPassword(String password) {
    Validate.notBlank(password, "password cannot be null");
    put("password", password.trim());
  }

  void setJdbcUrl(String jdbcUrl) {
    Validate.notBlank(jdbcUrl, "url cannot be null");
    put("url", jdbcUrl.trim());
  }

  void setCachePrepStmts(Boolean cachePrepStmts) {
    Validate.notNull(cachePrepStmts, "cachePrepStmts cannot be null");
    put("cachePrepStmts", cachePrepStmts.toString());
  }

  void setPrepStmtCacheSize(Integer prepStmtCacheSize) {
    Validate.notNull(prepStmtCacheSize, "prepStmtCacheSize cannot be null");
    put("prepStmtCacheSize", prepStmtCacheSize.toString());
  }

  void setPrepStmtCacheSqlLimit(Integer prepStmtCacheSqlLimit) {
    Validate.notNull(prepStmtCacheSqlLimit, "prepStmtCacheSqlLimit cannot be null");
    put("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit.toString());
  }
}
