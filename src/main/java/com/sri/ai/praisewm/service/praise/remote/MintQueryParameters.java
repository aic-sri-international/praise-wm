package com.sri.ai.praisewm.service.praise.remote;

public class MintQueryParameters {
  private String standardName = "one_month_time_integral_of_precipitation_leq_volume_flux";
  private String startTime = "1999-04-01T00:00:00";
  private String endTime = "1999-04-30T23:59:59";
  private String location = "40,0,55,15";

  public String getStandardName() {
    return standardName;
  }

  public MintQueryParameters setStandardName(String standardName) {
    this.standardName = standardName;
    return this;
  }

  public String getStartTime() {
    return startTime;
  }

  public MintQueryParameters setStartTime(String startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getEndTime() {
    return endTime;
  }

  public MintQueryParameters setEndTime(String endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getLocation() {
    return location;
  }

  public MintQueryParameters setLocation(String location) {
    this.location = location;
    return this;
  }
}
