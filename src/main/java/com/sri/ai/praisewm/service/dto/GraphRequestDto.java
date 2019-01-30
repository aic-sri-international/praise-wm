package com.sri.ai.praisewm.service.dto;

import java.util.List;
import java.util.Objects;

public class GraphRequestDto {
  private String xmVariable;
  // Any enums in the list will be the single value result of a client selection
  private List<GraphVariableSet> graphVariableSets;

  // no-arg constructor for JSON conversion
  public GraphRequestDto() {}

  public String getXmVariable() {
    return xmVariable;
  }

  public GraphRequestDto setXmVariable(String xmVariable) {
    this.xmVariable = xmVariable;
    return this;
  }

  public List<GraphVariableSet> getGraphVariableSets() {
    return graphVariableSets;
  }

  public GraphRequestDto setGraphVariableSets(List<GraphVariableSet> graphVariableSets) {
    this.graphVariableSets = graphVariableSets;
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
    GraphRequestDto that = (GraphRequestDto) o;
    return Objects.equals(xmVariable, that.xmVariable) &&
        Objects.equals(graphVariableSets, that.graphVariableSets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xmVariable, graphVariableSets);
  }

  @Override
  public String toString() {
    return "GraphRequestDto{"
        + "xmVariable='"
        + xmVariable
        + '\''
        + ", graphVariableSets="
        + graphVariableSets
        + '}';
  }
}
