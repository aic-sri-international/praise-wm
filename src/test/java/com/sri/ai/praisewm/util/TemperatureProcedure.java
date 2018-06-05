package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.Procedure;
import com.sri.ai.praisewm.service.praise.ProcedurePayload;

public class TemperatureProcedure implements Procedure {
  private CropYieldPredictionQuery querySolver;

  public TemperatureProcedure(CropYieldPredictionQuery querySolver) {
    this.querySolver = querySolver;
  }

  public TemperatureProcedureResult evaluate(ProcedurePayload payload) {
    return querySolver.solve(TemperatureProcedurePayload.class.cast(payload));
  }
}
