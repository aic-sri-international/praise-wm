package com.sri.ai.praisewm.util;

public class CropYieldPredictionQuery {
  public TemperatureProcedureResult solve(TemperatureProcedurePayload payload) {
    String jsonPayload = JsonConverter.to(payload);

    // HTTP post to CropYieldPrediction server ('api/' + ProcedureNames.TEMPERATURE, jsonPayload)
    // remote server computes and returns result as JSON
    TemperatureProcedureResult result =
        new TemperatureProcedureResult().setTemperatureInCelsius(83.6f);
    String jsonResult = JsonConverter.to(result);

    // Convert remote JSON response
    return JsonConverter.from(jsonResult, TemperatureProcedureResult.class);
  }
}
