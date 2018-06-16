package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.ProcedurePayload;

public class TemperatureProcedureImpl extends AbstractTemperatureProcedure {
  private CropYieldPredictionQuery querySolver;

  public TemperatureProcedureImpl(CropYieldPredictionQuery querySolver) {
    this.querySolver = querySolver;
  }

  public TemperatureProcedureResult evaluate(ProcedurePayload payload) {
    return querySolver.solve(TemperatureProcedurePayload.class.cast(payload));
  }
}
