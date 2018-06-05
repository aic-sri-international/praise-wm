package com.sri.ai.praisewm.db.internal;

import com.sri.ai.praisewm.db.JooqContext;
import com.sri.ai.praisewm.db.JooqTxProcessor;
import java.util.function.Consumer;
import java.util.function.Function;

/** JooqTxProcessorImpl */
public class JooqTxProcessorImpl implements JooqTxProcessor {
  private JooqConnectionContextFactory ctxFactory;

  public JooqTxProcessorImpl(JooqConnectionContextFactory ctxFactory) {
    this.ctxFactory = ctxFactory;
  }

  @Override
  public void run(Consumer<JooqContext> jooqContext) {
    try (JooqConnectionContext jcx = ctxFactory.getJooqConnectionContext()) {
      JooqContext jc = jcx.getJooqContext();
      jooqContext.accept(jc);
      jcx.commit();
    }
  }

  @Override
  public <R> R query(Function<JooqContext, R> jooqContext) {
    R result;
    try (JooqConnectionContext jcx = ctxFactory.getJooqConnectionContext()) {
      JooqContext jc = jcx.getJooqContext();
      result = jooqContext.apply(jc);
      jcx.commit();
      return result;
    }
  }

  @Override
  public JooqConnectionContext newConnectionContext() {
    return ctxFactory.getJooqConnectionContext();
  }
}
