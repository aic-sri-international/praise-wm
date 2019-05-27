package com.sri.ai.praisewm.service.dto;

import java.util.List;
import java.util.Objects;

/**
 * Client request to be applied against a <code>Function</code> result from a prior HOGM Query
 * result.
 */
public class GraphRequestDto {
  private String xmVariable;
  // Any enums in the list will be the single value result of a client selection
  private List<GraphVariableSet> graphVariableSets;

  // no-arg constructor for JSON conversion
  public GraphRequestDto() {}

  /**
   * Get the x-axis variable to use
   *
   * @return the x-axis variable name
   */
  public String getXmVariable() {
    return xmVariable;
  }

  /**
   * Set the name of the x-axis variable to use
   *
   * @param xmVariable the name of the x-axis variable
   * @return this instance
   */
  public GraphRequestDto setXmVariable(String xmVariable) {
    this.xmVariable = xmVariable;
    return this;
  }

  /**
   * Get the list of {@link GraphVariableSet} entries to use
   *
   * @return the list of <code>GraphVariableSet</code> entries
   */
  public List<GraphVariableSet> getGraphVariableSets() {
    return graphVariableSets;
  }

  /**
   * Set the list of {@link GraphVariableSet} entries to use
   *
   * @param graphVariableSets the list of <code></code>GraphVariableSet}</code> entries
   * @return this instance
   */
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
    return Objects.equals(xmVariable, that.xmVariable)
        && Objects.equals(graphVariableSets, that.graphVariableSets);
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
