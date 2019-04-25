package com.sri.ai.praisewm.service.dto;

import java.util.List;

public class ModelPageDto {
  private String model;
  private List<String> queries;

  // no-arg constructor for JSON conversion
  public ModelPageDto() {}

  public String getModel() {
    return model;
  }

  public ModelPageDto setModel(String model) {
    this.model = model;
    return this;
  }

  public List<String> getQueries() {
    return queries;
  }

  public ModelPageDto setQueries(List<String> queries) {
    this.queries = queries;
    return this;
  }

  @Override
  public String toString() {
    return "ModelPageDto{" + "model='" + model + '\'' + ", queries=" + queries + '}';
  }
}
