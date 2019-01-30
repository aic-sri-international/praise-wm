package com.sri.ai.praisewm.service.dto;

import java.util.Map;

public class GraphRequestResultDto {
  private String imageData;
  private Map<String, Double> mapRegionNameToValue;

  public String getImageData() {
    return imageData;
  }

  public GraphRequestResultDto setImageData(String imageData) {
    this.imageData = imageData;
    return this;
  }

  public Map<String, Double> getMapRegionNameToValue() {
    return mapRegionNameToValue;
  }

  public GraphRequestResultDto setMapRegionNameToValue(Map<String, Double> mapRegionNameToValue) {
    this.mapRegionNameToValue = mapRegionNameToValue;
    return this;
  }
}
