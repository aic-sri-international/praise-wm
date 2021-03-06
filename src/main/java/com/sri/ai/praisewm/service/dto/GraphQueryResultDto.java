package com.sri.ai.praisewm.service.dto;

import java.util.List;
import java.util.Map;

/**
 * GraphQueryResultDto represents data contained within and derived from a HOGM query <code>Function
 * </code> result.
 */
public class GraphQueryResultDto {
  // Not empty
  // The 1st entry in the list is the xm variable used to create the imageData
  // Graph creation is supported for others in the list
  private List<String> xmVariables;

  // Not empty
  // Contains an entry for each variable.
  private List<GraphVariableSet> graphVariableSets;

  // bas64 image with data/mime header
  private String imageData;

  // Map region to value
  private Map<String, Double> mapRegionNameToValue;

  // no-arg constructor for JSON conversion
  public GraphQueryResultDto() {}

  public List<String> getXmVariables() {
    return xmVariables;
  }

  public GraphQueryResultDto setXmVariables(List<String> xmVariables) {
    this.xmVariables = xmVariables;
    return this;
  }

  public List<GraphVariableSet> getGraphVariableSets() {
    return graphVariableSets;
  }

  public GraphQueryResultDto setGraphVariableSets(List<GraphVariableSet> graphVariableSets) {
    this.graphVariableSets = graphVariableSets;
    return this;
  }

  public String getImageData() {
    return imageData;
  }

  public GraphQueryResultDto setImageData(String imageData) {
    this.imageData = imageData;
    return this;
  }

  public Map<String, Double> getMapRegionNameToValue() {
    return mapRegionNameToValue;
  }

  public GraphQueryResultDto setMapRegionNameToValue(Map<String, Double> mapRegionNameToValue) {
    this.mapRegionNameToValue = mapRegionNameToValue;
    return this;
  }

  @Override
  public String toString() {
    return "GraphQueryResultDto{"
        + "xmVariables="
        + xmVariables
        + ", graphVariableSets="
        + graphVariableSets
        + ", imageData="
        + (imageData == null ? null : imageData.substring(0, 20))
        + ", mapRegionNameToValue="
        + mapRegionNameToValue
        + '}';
  }
}
