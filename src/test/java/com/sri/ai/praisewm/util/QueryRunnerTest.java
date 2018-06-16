package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.ProceduralAttachmentsImpl;
import com.sri.ai.praisewm.service.praise.ProcedureNames;
import org.junit.Test;

public class QueryRunnerTest {
  @Test
  public void main() {
    // Communicates with remote system to solve query
    CropYieldPredictionQuery cypq = new CropYieldPredictionQuery();

    ProceduralAttachmentsImpl pai = new ProceduralAttachmentsImpl();
    pai.put(ProcedureNames.TEMPERATURE, new TemperatureProcedureImpl(cypq));

    new QueryRunner("model string...", "query string...", pai);
  }
}
