package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.solver.HOGMProblemResult;
import com.sri.ai.praisewm.service.ServiceManager;
import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.service.dto.GraphVariableRangeDto;
import com.sri.ai.praisewm.service.dto.GraphVariableSet;
import com.sri.ai.util.graph2d.api.graph.GraphSetMaker;
import com.sri.ai.util.graph2d.api.variables.SetOfValues;
import com.sri.ai.util.graph2d.api.variables.Value;
import com.sri.ai.util.graph2d.api.variables.Variable;
import com.sri.ai.util.graph2d.core.values.SetOfEnumValues;
import com.sri.ai.util.graph2d.core.values.SetOfIntegerValues;
import com.sri.ai.util.graph2d.core.values.SetOfRealValues;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphManager {
  private final GraphCache graphCache;

  public GraphManager(ServiceManager serviceManager) {
    this.graphCache = new GraphCache(serviceManager.getEventBus());
  }

  private static List<GraphVariableSet> buildGraphVariableSetList(GraphSetMaker graphSetMaker) {
    List<GraphVariableSet> list = new ArrayList<>();

    Map<Variable, SetOfValues> map = graphSetMaker.getFromVariableToSetOfValues();
    map.forEach(
        (variable, setOfValues) -> {
          GraphVariableSet graphVariableSet = new GraphVariableSet();
          graphVariableSet.setName(variable.getName());

          if (setOfValues instanceof SetOfEnumValues) {
            List<String> enums;
            SetOfEnumValues values = (SetOfEnumValues) setOfValues;
            enums = new ArrayList<>();
            Iterator<Value> iter = values.iterator();
            while (iter.hasNext()) {
              enums.add(iter.next().stringValue());
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

          list.add(graphVariableSet);
        });
    return list;
  }

  private static GraphVariableSet toSingleFirstEntryVariableSet(GraphVariableSet variableSet) {
    if (variableSet.getEnums() != null) {
      return new GraphVariableSet()
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

    // @TODO build the graph and set the base64 value into the cached GraphQueryResultDto
    return result;
  }

  public GraphQueryResultDto setGraphQueryResult(String sessionId, HOGMProblemResult result) {
    // @TODO replace the following with data from the result once it is available
    SampleGraphQuery sampleGraphQuery = new SampleGraphQuery();
    GraphSetMaker graphSetMaker = sampleGraphQuery.getGraphSetMaker();
    List<Variable> xmVariables = sampleGraphQuery.getXmVariables();

    GraphCacheEntry entry = new GraphCacheEntry();
    entry.setGraphSetMaker(graphSetMaker);

    GraphQueryResultDto graphQueryResultDto = new GraphQueryResultDto();
    graphQueryResultDto.setXmVariables(
        xmVariables.stream().map(Variable::getName).collect(Collectors.toList()));
    graphQueryResultDto.setGraphVariableSets(buildGraphVariableSetList(graphSetMaker));

    entry.setGraphQueryResultDto(graphQueryResultDto);

    graphCache.addEntry(sessionId, entry);

    // Build GraphRequestDto to get the image, then set it into the GraphQueryResultDto
    // and return it to the caller.
    GraphRequestDto graphRequestDto = new GraphRequestDto();
    graphRequestDto.setXmVariable(graphQueryResultDto.getXmVariables().get(0));

    List<GraphVariableSet> list = new ArrayList<>();
    graphRequestDto
        .getGraphVariableSets()
        .forEach(
            s -> {
              if (!s.getName().equals(graphRequestDto.getXmVariable())) {
                list.add(toSingleFirstEntryVariableSet(s));
              }
            });
    graphRequestDto.setGraphVariableSets(list);

    // handleGraphRequest will set the graph image field for our cached GraphQueryResultDto instance.
    handleGraphRequest(sessionId, graphRequestDto);

    return graphQueryResultDto;
  }
}
