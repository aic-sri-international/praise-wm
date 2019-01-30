package com.sri.ai.praisewm.service.dto;

public class ModelQueryDto {
  private String query;
  private String model;
  private Integer numberOfInitialSamples;
  private Integer numberOfDiscreteValues;

  // no-arg constructor for JSON conversion
  public ModelQueryDto() {}

  public String getQuery() {
    return query;
  }

  public ModelQueryDto setQuery(String query) {
    this.query = query;
    return this;
  }

  public String getModel() {
    return model;
  }

  public ModelQueryDto setModel(String model) {
    this.model = model;
    return this;
  }

  public Integer getNumberOfInitialSamples() {
    return numberOfInitialSamples;
  }

  public ModelQueryDto setNumberOfInitialSamples(Integer numberOfInitialSamples) {
    this.numberOfInitialSamples = numberOfInitialSamples;
    return this;
  }

  public Integer getNumberOfDiscreteValues() {
    return numberOfDiscreteValues;
  }

  public ModelQueryDto setNumberOfDiscreteValues(Integer numberOfDiscreteValues) {
    this.numberOfDiscreteValues = numberOfDiscreteValues;
    return this;
  }

  @Override
  public String toString() {
    return "ModelQueryDto{"
        + "query='"
        + query
        + '\''
        + ", model='"
        + model
        + '\''
        + ", numberOfInitialSamples="
        + numberOfInitialSamples
        + ", numberOfDiscreteValues="
        + numberOfDiscreteValues
        + '}';
  }
}
