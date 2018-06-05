package com.sri.ai.praisewm.db;

import org.jooq.Configuration;
import org.jooq.DSLContext;

/** JooqContext. */
public interface JooqContext {
  DSLContext dslContext();

  Configuration configuration();

  ConnectionContext connectionContext();
}
