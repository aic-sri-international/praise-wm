package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling.ExpressionWithProbabilityFunction;
import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.util.function.api.functions.Function;
import com.sri.ai.util.function.api.functions.Functions;
import com.sri.ai.util.function.api.variables.SetOfValues;
import com.sri.ai.util.function.api.variables.SetOfVariables;
import com.sri.ai.util.function.api.variables.Variable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

public class QueryFunctionCacheEntry {
  private final ExpressionWithProbabilityFunction expressionWithProbabilityFunction;
  private Map<Variable, SetOfValues> mapOfVariableToSetOfValues;
  private List<Variable> xmVariables;
  private GraphQueryResultDto graphQueryResultDto;
  private GraphRequestDto lastRequest;

  public QueryFunctionCacheEntry(
      ExpressionWithProbabilityFunction expressionWithProbabilityFunction) {
    this.expressionWithProbabilityFunction = expressionWithProbabilityFunction;
  }

  public Function getFunction() {
    return Objects.requireNonNull(
        expressionWithProbabilityFunction
            .getDiscretizedConditionalProbabilityDistributionFunction(),
        "Expression returned a null function");
  }

  public Functions getFunctions() {
    return Functions.functions(getFunction());
  }

  public Map<Variable, SetOfValues> getOriginalMapOfVariableToSetOfValues() {
    if (mapOfVariableToSetOfValues == null) {
      mapOfVariableToSetOfValues = buildMapOfVariableToSetOfValues();
    }

    return mapOfVariableToSetOfValues;
  }

  private Map<Variable, SetOfValues> buildMapOfVariableToSetOfValues() {
    Map<Variable, SetOfValues> variableToSetOfValues = new LinkedHashMap<>();

    SetOfVariables setOfVariables = getFunction().getSetOfInputVariables();
    ArrayList<? extends Variable> variables = setOfVariables.getVariables();
    variables.forEach(
        variable -> {
          variableToSetOfValues.put(variable, variable.getSetOfValuesOrNull());
        });

    return variableToSetOfValues;
  }

  List<String> getXmVariableNames() {
    if (xmVariables == null) {
      xmVariables = buildXmVariables();
    }
    return xmVariables.stream().map(Variable::getName).collect(Collectors.toList());
  }

  private List<Variable> buildXmVariables() {
    // Currently there is only one possible xm variable.
    List<Variable> xmVariables = new ArrayList<>();

    SetOfVariables setOfVariables = getFunction().getSetOfInputVariables();
    ArrayList<? extends Variable> variables = setOfVariables.getVariables();

    int queryIndex =
        expressionWithProbabilityFunction
            .getDiscretizedConditionalProbabilityDistributionFunctionQueryIndex();

    Validate.inclusiveBetween(
        0,
        variables.size() - 1,
        queryIndex,
        String.format(
            "getDiscretizedConditionalProbabilityDistributionFunctionQueryIndex=%d, #variables=%d",
            queryIndex, variables.size()));

    Functions functions = Functions.functions(getFunction());

    Variable xmVariable = functions.getAllInputVariables().getVariables().get(queryIndex);
    if (xmVariable == null) {
      throw new RuntimeException("xmVariable not found for queryIndex=" + queryIndex);
    }

    xmVariables.add(xmVariable);

    return xmVariables;
  }

  GraphQueryResultDto getGraphQueryResultDto() {
    return graphQueryResultDto;
  }

  QueryFunctionCacheEntry setGraphQueryResultDto(GraphQueryResultDto graphQueryResultDto) {
    this.graphQueryResultDto = graphQueryResultDto;
    return this;
  }

  GraphRequestDto getLastRequest() {
    return lastRequest;
  }

  QueryFunctionCacheEntry setLastRequest(GraphRequestDto lastRequest) {
    this.lastRequest = lastRequest;
    return this;
  }

  Variable getVariableByName(String name) {
    Set<Variable> variables = getOriginalMapOfVariableToSetOfValues().keySet();
    List<Variable> list =
        variables.stream().filter(v -> v.getName().equals(name)).collect(Collectors.toList());
    if (list.size() != 1) {
      throw new RuntimeException(
          String.format("Variable name '%s' found %d times in '%s'", name, list.size(), variables));
    }
    return list.get(0);
  }
}
