package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.util.function.api.functions.Functions;
import com.sri.ai.util.function.api.variables.SetOfValues;
import com.sri.ai.util.function.api.variables.Variable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphCacheEntry {
  private final Functions functions;
  private final Map<Variable, SetOfValues> mapOfVariableToSetOfValues;
  private final List<Variable> xmVariables;
  private GraphQueryResultDto graphQueryResultDto;
  private GraphRequestDto lastRequest;

  public GraphCacheEntry(
      Functions functions,
      Map<Variable, SetOfValues> mapOfVariableToSetOfValues,
      List<Variable> xmVariables) {
    this.functions = functions;
    this.mapOfVariableToSetOfValues = mapOfVariableToSetOfValues;
    this.xmVariables = xmVariables;
  }

  public Functions getFunctions() {
    return functions;
  }

  public Map<Variable, SetOfValues> getMapOfVariableToSetOfValues() {
    return mapOfVariableToSetOfValues;
  }

  public List<Variable> getXmVariables() {
    return xmVariables;
  }

  public GraphQueryResultDto getGraphQueryResultDto() {
    return graphQueryResultDto;
  }

  public GraphCacheEntry setGraphQueryResultDto(GraphQueryResultDto graphQueryResultDto) {
    this.graphQueryResultDto = graphQueryResultDto;
    return this;
  }

  public GraphRequestDto getLastRequest() {
    return lastRequest;
  }

  public GraphCacheEntry setLastRequest(GraphRequestDto lastRequest) {
    this.lastRequest = lastRequest;
    return this;
  }

  public Variable getVariableByName(String name) {
    Set<Variable> variables = mapOfVariableToSetOfValues.keySet();
    List<Variable> list =
        variables.stream().filter(v -> v.getName().equals(name)).collect(Collectors.toList());
    if (list.size() != 1) {
      throw new RuntimeException(
          String.format("Variable name '%s' found %d times in '%s'", name, list.size(), variables));
    }
    return list.get(0);
  }
}
