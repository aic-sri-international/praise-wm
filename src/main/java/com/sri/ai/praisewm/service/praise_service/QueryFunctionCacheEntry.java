package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.util.function.api.functions.Function;
import com.sri.ai.util.function.api.variables.SetOfValues;
import com.sri.ai.util.function.api.variables.Variable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

public class QueryFunctionCacheEntry {
  private final Function function;
  private final Map<Variable, SetOfValues> mapOfVariableToSetOfValues;
  private final List<Variable> xmVariables;
  private GraphQueryResultDto graphQueryResultDto;
  private GraphRequestDto lastRequest;
  private String queryText;

  public QueryFunctionCacheEntry(
      Function function,
      Map<Variable, SetOfValues> mapOfVariableToSetOfValues,
      List<Variable> xmVariables,
      String queryText) {
    this.function = function;
    this.mapOfVariableToSetOfValues = mapOfVariableToSetOfValues;
    this.xmVariables = xmVariables;
    this.queryText = queryText;
  }

  public Function getFunction() {
    return function;
  }

  public Map<Variable, SetOfValues> getMapOfVariableToSetOfValues() {
    return mapOfVariableToSetOfValues;
  }

  public Variable getCurrentXmVariable() {
    return Validate.notEmpty(xmVariables, "xmVariables variables list is empty").get(0);
  }

  public String getQueryText() {
    return queryText;
  }

  public List<Variable> getXmVariables() {
    return xmVariables;
  }

  public GraphQueryResultDto getGraphQueryResultDto() {
    return graphQueryResultDto;
  }

  public QueryFunctionCacheEntry setGraphQueryResultDto(GraphQueryResultDto graphQueryResultDto) {
    this.graphQueryResultDto = graphQueryResultDto;
    return this;
  }

  public GraphRequestDto getLastRequest() {
    return lastRequest;
  }

  public QueryFunctionCacheEntry setLastRequest(GraphRequestDto lastRequest) {
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
