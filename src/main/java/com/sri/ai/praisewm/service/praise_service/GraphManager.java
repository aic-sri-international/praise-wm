package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.solver.HOGMProblemResult;
import com.sri.ai.praisewm.service.ServiceManager;
import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.service.dto.GraphVariableRangeDto;
import com.sri.ai.praisewm.service.dto.GraphVariableSet;
import com.sri.ai.praisewm.util.FilesUtil;
import com.sri.ai.praisewm.web.error.ProcessingException;
import com.sri.ai.util.graph2d.api.graph.GraphPlot;
import com.sri.ai.util.graph2d.api.graph.GraphSet;
import com.sri.ai.util.graph2d.api.graph.GraphSetMaker;
import com.sri.ai.util.graph2d.api.variables.SetOfValues;
import com.sri.ai.util.graph2d.api.variables.Unit;
import com.sri.ai.util.graph2d.api.variables.Value;
import com.sri.ai.util.graph2d.api.variables.Variable;
import com.sri.ai.util.graph2d.core.values.SetOfEnumValues;
import com.sri.ai.util.graph2d.core.values.SetOfIntegerValues;
import com.sri.ai.util.graph2d.core.values.SetOfRealValues;
import com.sri.ai.util.graph2d.core.variables.EnumVariable;
import com.sri.ai.util.graph2d.core.variables.IntegerVariable;
import com.sri.ai.util.graph2d.core.variables.RealVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphManager {
  private static final Logger LOG = LoggerFactory.getLogger(GraphManager.class);

  private final GraphCache graphCache;

  public GraphManager(ServiceManager serviceManager) {
    this.graphCache = new GraphCache(serviceManager.getEventBus());
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

  private static void setUnitValues(Variable variable, GraphVariableRangeDto graphVariableRangeDto) {
    Unit unit = variable.getUnit();
    if (unit != null) {
      graphVariableRangeDto.setUnitName(unit.getName());
      graphVariableRangeDto.setUnitSymbol(unit.getSymbol());
    }
  }

  private static SetOfValues buildSetOfValuesForVariable(
      Variable variable,
      GraphVariableSet graphVariableSet) {
      SetOfValues setOfValues = null;

      graphVariableSet.setName(variable.getName());

      if (variable instanceof EnumVariable) {
        Validate.notEmpty(graphVariableSet.getEnums(),
            "variable instanceof EnumVariable: invalid GraphVariableSet " + graphVariableSet);
        setOfValues = SetOfEnumValues.setOfEnumValues(graphVariableSet.getEnums().toArray(new String[0]));
      } else if (variable instanceof IntegerVariable) {
        GraphVariableRangeDto rangeDto = graphVariableSet.getRange();
        Validate.notNull(rangeDto,
            "variable instanceof IntegerVariable: invalid GraphVariableSet " + graphVariableSet);
        setOfValues = new SetOfIntegerValues((int) rangeDto.getFirst(), (int) rangeDto.getLast());
      } else if (variable instanceof RealVariable) {
        GraphVariableRangeDto rangeDto = graphVariableSet.getRange();
        Validate.notNull(rangeDto,
            "variable instanceof RealVariable: invalid GraphVariableSet " + graphVariableSet);
        setOfValues = new SetOfRealValues(
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
      List<GraphVariableSet> graphVariableSets,
      GraphCacheEntry entry) {
    Map<Variable, SetOfValues> map = new LinkedHashMap<>();

    for (GraphVariableSet variableSet : graphVariableSets) {
      Variable variable = entry.getVariableByName(variableSet.getName());
      SetOfValues setOfValues = buildSetOfValuesForVariable(variable, variableSet);
      map.put(variable, setOfValues);
    }

    return map;
  }

  private static GraphVariableSet toSingleFirstEntryVariableSet(GraphVariableSet variableSet) {
    if (variableSet.getEnums() != null) {
      return new GraphVariableSet()
          .setName(variableSet.getName())
          .setEnums(Collections.singletonList(variableSet.getEnums().get(0)));
    }

    return variableSet;
  }

  public GraphRequestResultDto handleGraphRequest(
      String sessionId, GraphRequestDto graphRequestDto) {
    GraphRequestResultDto result = new GraphRequestResultDto();
    GraphCacheEntry entry = graphCache.getEntry(sessionId);
    if (graphRequestDto.equals(entry.getLastRequest())) {
      // If it's the same request, return the prior image
      return new GraphRequestResultDto().setImageData(entry.getGraphQueryResultDto().getImageData());
    }

    GraphSetMaker graphSetMaker = GraphSetMaker.graphSetMaker();
    graphSetMaker.setFunctions(entry.getFunctions());
    graphSetMaker.setFromVariableToSetOfValues(
        buildNewVariableToSetOfValuesMap(graphRequestDto.getGraphVariableSets(), entry));
    Variable xmVariable = entry.getVariableByName(graphRequestDto.getXmVariable());
    GraphSet graphSet = graphSetMaker.make(xmVariable);
    List<? extends GraphPlot> graphPlots = graphSet.getGraphPlots();
    if (graphPlots.size() > 1) {
      LOG.warn("Multiple GraphPlots generated, only the first will be used");
    } else if (graphPlots.isEmpty()) {
      LOG.error("GraphPlot did not get generated");
    }

    if (!graphPlots.isEmpty()) {
      GraphPlot graphPlot = graphPlots.get(0);
      result.setImageData(FilesUtil.imageFileToBase64DataImage(graphPlot.getImageFile().toPath()));
      entry.getGraphQueryResultDto().setImageData(result.getImageData());
      entry.setLastRequest(graphRequestDto);
    }

    return result;
  }

  public GraphQueryResultDto setGraphQueryResult(String sessionId, HOGMProblemResult result) {
    try {
      return setGraphQueryResult_internal(sessionId, result);
    } catch (Exception ex) {
      LOG.error("Cannot generate graph", ex);
    }
    return null;
  }

  private GraphQueryResultDto setGraphQueryResult_internal(String sessionId, HOGMProblemResult result) {
    // @TODO replace the following with data from the HOGMProblemResult once it is available
    SampleGraphQueryResult sampleResult = new SampleGraphQueryResult();

    GraphCacheEntry entry =
        new GraphCacheEntry(
            sampleResult.getFunctions(),
            sampleResult.getMapOfVariableToSetOfValues(),
            getPossibleXmVariables(sampleResult.getMapOfVariableToSetOfValues()));

    GraphQueryResultDto graphQueryResultDto = new GraphQueryResultDto();
    graphQueryResultDto.setXmVariables(
        entry.getXmVariables().stream().map(Variable::getName).collect(Collectors.toList()));
    graphQueryResultDto.setGraphVariableSets(
        buildGraphVariableSetList(entry.getMapOfVariableToSetOfValues()));

    entry.setGraphQueryResultDto(graphQueryResultDto);

    graphCache.addEntry(sessionId, entry);

    // Build GraphRequestDto to get the image, then set it into the GraphQueryResultDto
    // and return it to the caller.
    GraphRequestDto graphRequestDto = new GraphRequestDto();
    // Use the first xmVariable
    graphRequestDto.setXmVariable(graphQueryResultDto.getXmVariables().get(0));

    List<GraphVariableSet> list = new ArrayList<>();
    for (GraphVariableSet s : graphQueryResultDto
        .getGraphVariableSets()) {
      list.add(toSingleFirstEntryVariableSet(s));
    }
    graphRequestDto.setGraphVariableSets(list);

    // handleGraphRequest will set the graph image field for our cached GraphQueryResultDto
    // instance.
    handleGraphRequest(sessionId, graphRequestDto);

    return graphQueryResultDto;
  }

  private static List<Variable> getPossibleXmVariables(Map<Variable, SetOfValues> mapOfVariableToSetOfValues) {
    List<Variable> xmVariables = new ArrayList<>();

    for (Map.Entry<Variable, SetOfValues> entry : mapOfVariableToSetOfValues.entrySet()) {
      if (entry.getValue() instanceof SetOfIntegerValues || entry.getValue() instanceof SetOfRealValues) {
        xmVariables.add(entry.getKey());
      }
    }
    if (xmVariables.isEmpty()) {
      throw new IllegalArgumentException("At least one of the SetOfValues must be a numeric type: "
          + mapOfVariableToSetOfValues);
    }

    return xmVariables;
  }
}