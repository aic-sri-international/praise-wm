package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.ProceduralAttachments;
import com.sri.ai.praisewm.service.praise.ProcedureNames;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryRunner {
  private static final Logger LOG = LoggerFactory.getLogger(QueryRunnerTest.class);

  public QueryRunner(String model, String query, ProceduralAttachments pa) {
    AbstractTemperatureProcedure p =
        AbstractTemperatureProcedure.class.cast(pa.get(ProcedureNames.TEMPERATURE));
    TemperatureProcedurePayload payload =
        new TemperatureProcedurePayload()
            .setDate(Instant.now())
            .setLatitude(38.8951f)
            .setLongitude(-77.0364f);

    TemperatureProcedureResult solution = p.evaluate(payload);
    LOG.info("Result for {}: {}\nPayload: {}", ProcedureNames.TEMPERATURE, solution, payload);
  }
}
