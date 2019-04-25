package com.sri.ai.praisewm.service.dto;

import java.util.Map;

/**
 * Contains the results of a successful aic-praise query result Function call.
 */
public class GraphRequestResultDto {
  private String imageData;
  private Map<String, Double> mapRegionNameToValue;

  /**
   * A base64 data image representation of a line graph or bar chart.
   *
   * @return the image data
   */
  public String getImageData() {
    return imageData;
  }

  /**
   * Set the image data.
   *
   * @param imageData a base64 data image representation of a line graph or bar chart.
   * @return this instance
   */
  public GraphRequestResultDto setImageData(String imageData) {
    this.imageData = imageData;
    return this;
  }

  /**
   * Region results map.
   *
   * @return a map of geographic regions with their associated result
   */
  public Map<String, Double> getMapRegionNameToValue() {
    return mapRegionNameToValue;
  }

  /**
   * Set the region to value map.
   *
   * @param mapRegionNameToValue the region to value map
   * @return this instance
   */
  public GraphRequestResultDto setMapRegionNameToValue(Map<String, Double> mapRegionNameToValue) {
    this.mapRegionNameToValue = mapRegionNameToValue;
    return this;
  }
}
