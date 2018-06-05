package com.sri.ai.praisewm.service.dto;

public class ModelQueryDto {
  private String query;
  private String model;

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

  @Override
  public String toString() {
    return "ModelQuery{" + "query='" + query + '\'' + ", model='" + model + '\'' + '}';
  }
}
