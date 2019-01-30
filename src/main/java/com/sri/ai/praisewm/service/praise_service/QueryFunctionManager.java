package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling.ExpressionWithProbabilityFunction;
import com.sri.ai.praisewm.service.ServiceManager;
import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.service.dto.GraphVariableRangeDto;
import com.sri.ai.praisewm.service.dto.GraphVariableSet;
import com.sri.ai.praisewm.util.FilesUtil;
import com.sri.ai.util.Util;
import com.sri.ai.util.function.api.functions.Function;
import com.sri.ai.util.function.api.functions.Functions;
import com.sri.ai.util.function.api.values.Value;
import com.sri.ai.util.function.api.variables.SetOfValues;
import com.sri.ai.util.function.api.variables.SetOfVariables;
import com.sri.ai.util.function.api.variables.Unit;
import com.sri.ai.util.function.api.variables.Variable;
import com.sri.ai.util.function.core.values.SetOfEnumValues;
import com.sri.ai.util.function.core.values.SetOfIntegerValues;
import com.sri.ai.util.function.core.values.SetOfRealValues;
import com.sri.ai.util.function.core.variables.DefaultAssignment;
import com.sri.ai.util.function.core.variables.DefaultSetOfVariables;
import com.sri.ai.util.function.core.variables.EnumVariable;
import com.sri.ai.util.function.core.variables.IntegerVariable;
import com.sri.ai.util.function.core.variables.RealVariable;
import com.sri.ai.util.graph2d.api.GraphPlot;
import com.sri.ai.util.graph2d.api.GraphSetMaker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryFunctionManager {
  private static final Logger LOG = LoggerFactory.getLogger(QueryFunctionManager.class);

  private final QueryFunctionCache queryFunctionCache;

  public QueryFunctionManager(ServiceManager serviceManager) {
    this.queryFunctionCache = new QueryFunctionCache(serviceManager.getEventBus());
  }

  private static List<GraphVariableSet> buildGraphVariableSetList(
      Map<Variable, SetOfValues> variableToSetOfValues) {
    List<GraphVariableSet> list = new ArrayList<>();

    variableToSetOfValues.forEach(
        (variable, setOfValues) -> {
          GraphVariableSet graphVariableSet = new GraphVariableSet();
          graphVariableSet.setName(variable.getName());

          if (setOfValues instanceof SetOfEnumValues) {
            List<String> enums = new ArrayList<>();
            for (Value value : setOfValues) {
              enums.add(value.stringValue());
            }
            graphVariableSet.setEnums(enums);
          } else if (setOfValues instanceof SetOfIntegerValues) {
            SetOfIntegerValues values = (SetOfIntegerValues) setOfValues;
            graphVariableSet.setRange(
                new GraphVariableRangeDto()
                    .setFirst(values.getFirst())
                    .setLast(values.getLast())
                    .setStep(1));
          } else if (setOfValues instanceof SetOfRealValues) {
            SetOfRealValues values = (SetOfRealValues) setOfValues;
            graphVariableSet.setRange(
                new GraphVariableRangeDto()
                    .setFirst(values.getFirst().doubleValue())
                    .setLast(values.getLast().doubleValue())
                    .setStep(values.getStep().doubleValue()));
          } else {
            throw new IllegalArgumentException(
                "Unsupported SetOfValues type:" + setOfValues.getClass().getName());
          }

          if (graphVariableSet.getRange() != null) {
            setUnitValues(variable, graphVariableSet.getRange());
          }
          list.add(graphVariableSet);
        });
    return list;
  }

  private static void setUnitValues(
      Variable variable, GraphVariableRangeDto graphVariableRangeDto) {
    Unit unit = variable.getUnit();
    if (unit != null && unit.getName() != null && !unit.getName().equals(Unit.NONE.getName())) {
      graphVariableRangeDto.setUnitName(unit.getName());
      graphVariableRangeDto.setUnitSymbol(StringUtils.trimToNull(unit.getSymbol()));
    }
  }

  private static SetOfValues buildSetOfValuesForVariable(
      Variable variable, GraphVariableSet graphVariableSet) {
    SetOfValues setOfValues;

    graphVariableSet.setName(variable.getName());

    if (variable instanceof EnumVariable) {
      Validate.notEmpty(
          graphVariableSet.getEnums(),
          "variable instanceof EnumVariable: invalid GraphVariableSet " + graphVariableSet);
      setOfValues =
          SetOfEnumValues.setOfEnumValues(graphVariableSet.getEnums().toArray(new String[0]));
    } else if (variable instanceof IntegerVariable) {
      GraphVariableRangeDto rangeDto = graphVariableSet.getRange();
      Validate.notNull(
          rangeDto,
          "variable instanceof IntegerVariable: invalid GraphVariableSet " + graphVariableSet);
      setOfValues = new SetOfIntegerValues((int) rangeDto.getFirst(), (int) rangeDto.getLast());
    } else if (variable instanceof RealVariable) {
      GraphVariableRangeDto rangeDto = graphVariableSet.getRange();
      Validate.notNull(
          rangeDto,
          "variable instanceof RealVariable: invalid GraphVariableSet " + graphVariableSet);
      setOfValues =
          new SetOfRealValues(
              Double.toString(rangeDto.getFirst()),
              Double.toString(rangeDto.getStep()),
              Double.toString(rangeDto.getLast()));
    } else {
      throw new IllegalArgumentException(
          "Unsupported Variable type:" + variable.getClass().getName());
    }

    return setOfValues;
  }

  private static Map<Variable, SetOfValues> buildNewVariableToSetOfValuesMap(
      List<GraphVariableSet> graphVariableSets, QueryFunctionCacheEntry entry) {
    Map<Variable, SetOfValues> map = new LinkedHashMap<>();

    for (GraphVariableSet variableSet : graphVariableSets) {
      Variable variable = entry.getVariableByName(variableSet.getName());
      SetOfValues setOfValues = buildSetOfValuesForVariable(variable, variableSet);
      map.put(variable, setOfValues);
    }

    return map;
  }

  /**
   * Determine the current xmVariable, and any others that could potentially become the xmVariable.
   * <p>
   * The first entry in the list is the current xmVariable.
   *
   * @param function the function returned from the HOGM model query.
   * @param queryText the text used for the query
   * @return list of all possible xmVariables. The first is the current xmVariable, any others
   * are variables that the user could potentially select from to change the current xmVariable.
   */
  private static List<Variable> getPossibleXmVariables(Function function, String queryText) {
    // Currently there is only one possible xm variable.
    List<Variable> xmVariables = new ArrayList<>();

    SetOfVariables setOfVariables = function.getSetOfInputVariables();
    ArrayList<? extends Variable> variables = setOfVariables.getVariables();

    int queryIndex =
        Util.getIndexOfFirstSatisfyingPredicateOrMinusOne(
            variables,
            v -> {
              if (v == null) {
                throw new RuntimeException(
                    "Null variable found in setOfVariables, variables=" + variables);
              }
              String vn = v.getName();
              if (vn == null) {
                throw new RuntimeException(
                    "Null variable name for variable within variables=" + variables);
              }
              return v.getName().equals(queryText);
            });

    if (queryIndex == -1) {
      throw new RuntimeException(
          String.format("Query variable %s not found in variables %s", queryText, variables));
    }

    Functions functions = Functions.functions(function);

    Variable xmVariable = functions.getAllInputVariables().getVariables().get(queryIndex);
    if (xmVariable == null) {
      throw new RuntimeException("xmVariable not found for queryIndex=" + queryIndex);
    }

    xmVariables.add(xmVariable);

    return xmVariables;
  }

  private DefaultAssignment getAssignmentOnNonXAxisVariables(Function function, String query) {
    SetOfVariables inputVariables = function.getSetOfInputVariables();

    int queryIndex =
        Util.getIndexOfFirstSatisfyingPredicateOrMinusOne(
            inputVariables.getVariables(), v -> v.getName().equals(query));

    ArrayList<? extends Variable> nonXAxisVariables = inputVariables.getVariables();
    nonXAxisVariables.remove(queryIndex);
    SetOfVariables setOfNonXAxisVariables = new DefaultSetOfVariables(nonXAxisVariables);

    List<? extends Value> values =
        Util.mapIntoList(nonXAxisVariables, v -> v.getSetOfValuesOrNull().get(0));
    DefaultAssignment assignmentOnNonXAxisVariables =
        new DefaultAssignment(setOfNonXAxisVariables, values);

    return assignmentOnNonXAxisVariables;
  }

  public GraphRequestResultDto handleGraphRequest(
      String sessionId, GraphRequestDto graphRequestDto, boolean isFirstCall) {
    GraphRequestResultDto result = new GraphRequestResultDto();
    QueryFunctionCacheEntry entry = queryFunctionCache.getEntry(sessionId);
    if (graphRequestDto.equals(entry.getLastRequest())) {
      // If it's the same request, return the prior image or map data
      return new GraphRequestResultDto()
          .setImageData(entry.getGraphQueryResultDto().getImageData())
          .setMapRegionNameToValue(entry.getGraphQueryResultDto().getMapRegionNameToValue());
    }

    //    Functions functions = Functions.functions(entry.getFunction());
    //
    //    DefaultAssignment assignmentOnNonXAxisVariables
    //        = getAssignmentOnNonXAxisVariables(entry.getFunction(), entry.getQueryText());
    //    GraphSetMaker graphSetMaker = GraphSetMaker.graphSetMaker();
    //
    //    graphSetMaker.setFunctions(Functions.functions(entry.getFunction()));
    //    graphSetMaker.setFromVariableToSetOfValues(
    //        buildNewVariableToSetOfValuesMap(graphRequestDto.getGraphVariableSets(), entry));
    //    Variable xmVariable = entry.getVariableByName(graphRequestDto.getXmVariable());
    //
    //    SetOfVariables inputVariables = entry.getFunction().getSetOfInputVariables();
    //    List<? extends Value> values = Util.mapIntoList(inputVariables.getVariables(), v ->
    // v.getSetOfValuesOrNull().get(0));
    //    DefaultAssignment defaultAssignment = new DefaultAssignment(inputVariables, values);
    //    GraphPlot graphPlot = graphSetMaker.plot(defaultAssignment, xmVariable);
    Functions functions = Functions.functions(entry.getFunction());

    // @TODO  isFirstCall==false get map of Variable to Value -> but how is a range handled ?
    Map<Variable, SetOfValues> variableSetOfValuesMap =
        buildNewVariableToSetOfValuesMap(graphRequestDto.getGraphVariableSets(), entry);

    List<Variable> nonXAxisVariables = new ArrayList<>(variableSetOfValuesMap.keySet());
    int queryIndex =
        Util.getIndexOfFirstSatisfyingPredicateOrMinusOne(
            nonXAxisVariables, v -> v.getName().equals(entry.getQueryText()));
    nonXAxisVariables.remove(queryIndex);

    SetOfVariables setOfNonXAxisVariables = new DefaultSetOfVariables(nonXAxisVariables);

    // @TODO how do get get all values, and not just the first ?
    List<? extends Value> values =
        Util.mapIntoList(nonXAxisVariables, v -> v.getSetOfValuesOrNull().get(0));

    DefaultAssignment assignmentOnNonXAxisVariables =
        new DefaultAssignment(setOfNonXAxisVariables, values);

    Variable xmVariable = entry.getCurrentXmVariable();

    GraphSetMaker graphSetMaker = GraphSetMaker.graphSetMaker();
    graphSetMaker.setFunctions(functions);

    GraphPlot graphPlot = graphSetMaker.plot(assignmentOnNonXAxisVariables, xmVariable);
    if (graphPlot.getRegionToValue() != null) {
      result.setMapRegionNameToValue(graphPlot.getRegionToValue());
    }
    if (graphPlot.getImageFile() != null) {
      result.setImageData(FilesUtil.imageFileToBase64DataImage(graphPlot.getImageFile().toPath()));
    }
    entry
        .getGraphQueryResultDto()
        .setImageData(result.getImageData())
        .setMapRegionNameToValue(result.getMapRegionNameToValue());
    entry.setLastRequest(graphRequestDto);

    return result;
  }

  public GraphQueryResultDto processQueryResultFunction(
      String sessionId, String queryText, Function function) {
    try {
      return processQueryResultFunction_internal(
          sessionId, queryText, function);
    } catch (Exception ex) {
      LOG.error("Cannot generate graph", ex);
    }
    return null;
  }

  private Map<Variable, SetOfValues> getMapOfVariableToSetOfValues(Function function) {
    Map<Variable, SetOfValues> variableToSetOfValues = new LinkedHashMap<>();

    SetOfVariables setOfVariables = function.getSetOfInputVariables();
    ArrayList<? extends Variable> variables = setOfVariables.getVariables();
    variables.forEach(
        variable -> {
          variableToSetOfValues.put(variable, variable.getSetOfValuesOrNull());
        });

    return variableToSetOfValues;
  }

  private GraphQueryResultDto processQueryResultFunction_internal(
      String sessionId,
      String queryText,
      Function function) {
    Map<Variable, SetOfValues> variableToSetOfValues = getMapOfVariableToSetOfValues(function);

    QueryFunctionCacheEntry entry =
        new QueryFunctionCacheEntry(
            function,
            variableToSetOfValues,
            getPossibleXmVariables(function, queryText),
            queryText);

    GraphQueryResultDto graphQueryResultDto = new GraphQueryResultDto();
    graphQueryResultDto.setXmVariables(
        entry.getXmVariables().stream().map(Variable::getName).collect(Collectors.toList()));
    graphQueryResultDto.setGraphVariableSets(
        buildGraphVariableSetList(entry.getMapOfVariableToSetOfValues()));

    entry.setGraphQueryResultDto(graphQueryResultDto);

    queryFunctionCache.addEntry(sessionId, entry);

    // Build GraphRequestDto to get the image or map data, then set it into the GraphQueryResultDto
    // and return it to the caller.
    GraphRequestDto graphRequestDto = new GraphRequestDto();
    // Use the first xmVariable
    graphRequestDto.setXmVariable(graphQueryResultDto.getXmVariables().get(0));

    graphRequestDto.setGraphVariableSets(graphQueryResultDto.getGraphVariableSets());

    // handleGraphRequest will set the graph image field for our cached GraphQueryResultDto
    // instance.
    handleGraphRequest(sessionId, graphRequestDto, true);

    return graphQueryResultDto;
  }
}
