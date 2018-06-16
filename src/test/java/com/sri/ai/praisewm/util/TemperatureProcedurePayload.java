package com.sri.ai.praisewm.util;

import com.sri.ai.praisewm.service.praise.ProcedurePayload;
import java.time.Instant;

public class TemperatureProcedurePayload implements ProcedurePayload {
  private Instant date;
  private Float latitude;
  private Float longitude;

  public Instant getDate() {
    return date;
  }

  public TemperatureProcedurePayload setDate(Instant date) {
    this.date = date;
    return this;
  }

  public Float getLatitude() {
    return latitude;
  }

  public TemperatureProcedurePayload setLatitude(Float latitude) {
    this.latitude = latitude;
    return this;
  }

  public Float getLongitude() {
    return longitude;
  }

  public TemperatureProcedurePayload setLongitude(Float longitude) {
    this.longitude = longitude;
    return this;
  }

  @Override
  public String toString() {
    return "TemperatureProcedurePayload{"
        + "date="
        + date
        + ", latitude="
        + latitude
        + ", longitude="
        + longitude
        + '}';
  }
}
