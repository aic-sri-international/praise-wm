package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.ProcedureResult;

public class TemperatureProcedureResult implements ProcedureResult {
  private Float temperatureInCelsius;

  public Float getTemperatureInCelsius() {
    return temperatureInCelsius;
  }

  public TemperatureProcedureResult setTemperatureInCelsius(Float temperatureInCelsius) {
    this.temperatureInCelsius = temperatureInCelsius;
    return this;
  }

  @Override
  public String toString() {
    return "TemperatureProcedureResult{" + "temperatureInCelsius=" + temperatureInCelsius + '}';
  }
}
