package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.expresso.core.DefaultSymbol;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling.ExpressionWithProbabilityFunction;
import com.sri.ai.praisewm.service.ServiceManager;
import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.util.FilesUtil;
import com.sri.ai.util.function.api.variables.SetOfValues;
import com.sri.ai.util.function.api.variables.Variable;
import com.sri.ai.util.function.core.variables.DefaultAssignment;
import com.sri.ai.util.graph2d.api.GraphPlot;
import com.sri.ai.util.graph2d.api.GraphSetMaker;
import java.util.Map;

/** The QueryFunctionManager manages a HOGM query result Function request. */
public class QueryFunctionManager {
  private final QueryFunctionCache queryFunctionCache;

  public QueryFunctionManager(ServiceManager serviceManager) {
    this.queryFunctionCache = new QueryFunctionCache(serviceManager.getEventBus());
  }

  public GraphRequestResultDto handleGraphRequest(
      String sessionId, GraphRequestDto graphRequestDto) {
    GraphRequestResultDto result = new GraphRequestResultDto();
    QueryFunctionCacheEntry entry = queryFunctionCache.getEntry(sessionId);
    if (graphRequestDto.equals(entry.getLastRequest())) {
      // If it's the same request, return the prior image or map data
      return new GraphRequestResultDto()
          .setImageData(entry.getGraphQueryResultDto().getImageData())
          .setMapRegionNameToValue(entry.getGraphQueryResultDto().getMapRegionNameToValue());
    }

    // Map contains the original variable with its original SetOfValues contained within it
    // SetOfValues is a set of values built from GraphRequestDto
    Map<Variable, SetOfValues> variableSetOfValuesMap =
        GraphVariableSetUtil.buildNewVariableToSetOfValuesMap(
            graphRequestDto.getGraphVariableSets(), entry);

    Variable xAxisVariable =
        GraphVariableSetUtil.getXAxisVariable(
            variableSetOfValuesMap, graphRequestDto.getXmVariable());

    DefaultAssignment assignmentOnNonXAxisVariables =
        GraphVariableSetUtil.getAssignmentOnNonXAxisVariables(
            variableSetOfValuesMap, graphRequestDto.getXmVariable());

    GraphSetMaker graphSetMaker = GraphSetMaker.graphSetMaker();
    graphSetMaker.setDecimalFormatter((value) -> DefaultSymbol.createSymbol(value).toString());

    graphSetMaker.setFunctions(entry.getFunctions());

    GraphPlot graphPlot = graphSetMaker.plot(assignmentOnNonXAxisVariables, xAxisVariable);
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

  GraphQueryResultDto processQueryResultFunction(
      String sessionId, ExpressionWithProbabilityFunction expressionWithProbabilityFunction) {

    QueryFunctionCacheEntry entry = new QueryFunctionCacheEntry(expressionWithProbabilityFunction);

    GraphQueryResultDto graphQueryResultDto = new GraphQueryResultDto();
    graphQueryResultDto.setXmVariables(entry.getXmVariableNames());
    graphQueryResultDto.setGraphVariableSets(
        GraphVariableSetUtil.buildGraphVariableSetList(
            entry.getOriginalMapOfVariableToSetOfValues()));

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
    handleGraphRequest(sessionId, graphRequestDto);

    return graphQueryResultDto;
  }
}
