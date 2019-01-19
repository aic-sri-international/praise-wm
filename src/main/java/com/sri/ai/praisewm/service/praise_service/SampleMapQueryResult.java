package com.sri.ai.praisewm.service.praise_service;

import static com.sri.ai.util.Util.map;
import static com.sri.ai.util.function.api.functions.Function.function;
import static com.sri.ai.util.function.api.functions.Functions.functions;
import static com.sri.ai.util.function.api.values.Value.value;
import static com.sri.ai.util.function.api.variables.SetOfVariables.setOfVariables;
import static com.sri.ai.util.function.api.variables.Variable.enumVariable;
import static com.sri.ai.util.function.api.variables.Variable.integerVariable;
import static com.sri.ai.util.function.api.variables.Variable.realVariable;
import static com.sri.ai.util.function.core.values.SetOfEnumValues.setOfEnumValues;
import static com.sri.ai.util.function.core.values.SetOfIntegerValues.setOfIntegerValues;

import com.sri.ai.util.function.api.functions.Function;
import com.sri.ai.util.function.api.functions.Functions;
import com.sri.ai.util.function.api.variables.SetOfValues;
import com.sri.ai.util.function.api.variables.Unit;
import com.sri.ai.util.function.api.variables.Variable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SampleMapQueryResult {
  private final Functions functions;
  private final Map<Variable, SetOfValues> mapOfVariableToSetOfValues;
  private final List<Variable> xmVariables;

  public SampleMapQueryResult() {
    Variable continent = enumVariable("Continent");
    Variable age = integerVariable("Age", Unit.YEAR);
    Variable occupation = enumVariable("Occupation");
    Variable income = realVariable("Income", Unit.DOLLAR);
    Variable expense = realVariable("Expense", Unit.DOLLAR);

    Function incomeFunction =
        function(
            "Income",
            income,
            setOfVariables(continent, age, occupation),
            (assignment) -> {
              String continentValue = assignment.get(continent).stringValue();
              String occupationValue = assignment.get(occupation).stringValue();

              if (continentValue.equals("North America") || continentValue.equals("Europe")) {
                if (occupationValue.equals("Doctor")) {
                  int ageValue = assignment.get(age).intValue();
                  return ageValue > 40 ? value(200000) : value(150000);
                } else {
                  return value(100000);
                }
              } else {
                return value(50000);
              }
            });

    Function expenseFunction =
        function(
            "Expense",
            expense,
            setOfVariables(continent, age, occupation),
            (assignment) -> value(incomeFunction.evaluate(assignment).doubleValue() * 0.75));

    functions = functions(incomeFunction, expenseFunction);

    mapOfVariableToSetOfValues =
        map(
            continent, setOfEnumValues("North America", "Africa", "Europe"),
            age, setOfIntegerValues(18, 99),
            occupation, setOfEnumValues("Driver", "CEO", "Doctor"));

    xmVariables = Collections.singletonList(age);
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
}
