package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praisewm.service.dto.GraphVariableRangeDto;
import com.sri.ai.praisewm.service.dto.GraphVariableSet;
import com.sri.ai.util.Util;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * GraphVariableSetUtil data conversion utilities
 * <p>
 *   Contains methods to convert between HOGM query result <code>Function</code> variable/values and
 *   DTOs, and, to determine or change the x-axis variable.
 */
class GraphVariableSetUtil {
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

  static Map<Variable, SetOfValues> buildNewVariableToSetOfValuesMap(
      List<GraphVariableSet> graphVariableSets, QueryFunctionCacheEntry entry) {
    Map<Variable, SetOfValues> map = new LinkedHashMap<>();

    for (GraphVariableSet variableSet : graphVariableSets) {
      Variable variable = entry.getVariableByName(variableSet.getName());
      SetOfValues setOfValues = buildSetOfValuesForVariable(variable, variableSet);
      map.put(variable, setOfValues);
    }

    return map;
  }

  static List<GraphVariableSet> buildGraphVariableSetList(
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

  private static Variable transposeVariable(Variable oldVariable, SetOfValues newSetOfValues) {
    Variable newVariable;

    if (newSetOfValues instanceof SetOfEnumValues) {
      newVariable = new EnumVariable(oldVariable.getName(), (SetOfEnumValues) newSetOfValues);
    } else if (newSetOfValues instanceof SetOfRealValues) {
      newVariable =
          new RealVariable(
              oldVariable.getName(), oldVariable.getUnit(), (SetOfRealValues) newSetOfValues);
    } else if (newSetOfValues instanceof SetOfIntegerValues) {
      newVariable =
          new IntegerVariable(oldVariable.getName(), oldVariable.getUnit(), newSetOfValues);
    } else {
      throw new IllegalArgumentException(
          "Unsupported SetOfValues type: " + newSetOfValues.getClass().getName());
    }

    return newVariable;
  }

  static Variable getXAxisVariable(
      Map<Variable, SetOfValues> variableSetOfValuesMap, String xmVariableName) {
    Variable originalVariable = null;
    SetOfValues newSetOfValues = null;

    for (Map.Entry<Variable, SetOfValues> entry : variableSetOfValuesMap.entrySet()) {
      if (xmVariableName.equals(entry.getKey().getName())) {
        originalVariable = entry.getKey();
        newSetOfValues = entry.getValue();
        break;
      }
    }
    if (newSetOfValues == null) {
      throw new IllegalArgumentException(
          String.format("xmVariableName=%s, not found in set of Variables", xmVariableName));
    }

    return transposeVariable(originalVariable, newSetOfValues);
  }

  // Map contains the original variable with its orginal SetOfValues contained within it
  // SetOfValues is a set of values built from GraphRequestDto
  static DefaultAssignment getAssignmentOnNonXAxisVariables(
      Map<Variable, SetOfValues> variableSetOfValuesMap, String xmVariableName) {
    List<Variable> nonXAxisVariables = new ArrayList<>();

    for (Map.Entry<Variable, SetOfValues> entry : variableSetOfValuesMap.entrySet()) {
      if (!xmVariableName.equals(entry.getKey().getName())) {
        Variable originalVariable = entry.getKey();
        SetOfValues newSetOfValues = entry.getValue();
        nonXAxisVariables.add(transposeVariable(originalVariable, newSetOfValues));
      }
    }

    List<? extends Value> values =
        Util.mapIntoList(
            nonXAxisVariables,
            v ->
                Objects.requireNonNull(
                        v.getSetOfValuesOrNull(),
                        "nonXAxisVariable contains a null SetOfValues: " + v.getName())
                    .get(0));

    SetOfVariables setOfNonXAxisVariables = new DefaultSetOfVariables(nonXAxisVariables);

    return new DefaultAssignment(setOfNonXAxisVariables, values);
  }
}
