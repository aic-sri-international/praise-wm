package com.sri.ai.praisewm.service.dto;

import java.util.List;
import java.util.Objects;

public class GraphVariableSet {
  // Required
  private String name;
  // null if graphVariableRangeDto is non-null
  private List<String> enums;
  // null if enums is non-null
  private GraphVariableRangeDto range;

  // no-arg constructor for JSON conversion
  public GraphVariableSet() {}

  public String getName() {
    return name;
  }

  public GraphVariableSet setName(String name) {
    this.name = name;
    return this;
  }

  public List<String> getEnums() {
    return enums;
  }

  public GraphVariableSet setEnums(List<String> enums) {
    this.enums = enums;
    return this;
  }

  public GraphVariableRangeDto getRange() {
    return range;
  }

  public GraphVariableSet setRange(GraphVariableRangeDto range) {
    this.range = range;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphVariableSet that = (GraphVariableSet) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(enums, that.enums) &&
        Objects.equals(range, that.range);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, enums, range);
  }

  @Override
  public String toString() {
    return "GraphVariableSet{"
        + "name='"
        + name
        + '\''
        + ", enums="
        + enums
        + ", range="
        + range
        + '}';
  }
}
