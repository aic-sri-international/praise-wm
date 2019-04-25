package com.sri.ai.praisewm.service.dto;

import java.util.Arrays;

/**
 * Represents a model file with extended information.
 *
 * <p>The format supports dividing sections of a model into individual rules with optional
 * metadata and a set of example queries.
 * <p>The name field is mandatory.
 * <p>It is valid to include all declarations and rules (without metadata)
 * in the declarations field.
 * <p>Although it is optional to do so, it is strongly advised to include
 * at least one query to allow a user to easily test the model.
 */
public class SegmentedModelDto {
  private String name;
  private String description;
  private String declarations;
  private ModelRuleDto[] rules;
  private String[] queries;

  public String getName() {
    return name;
  }

  public SegmentedModelDto setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public SegmentedModelDto setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getDeclarations() {
    return declarations;
  }

  public SegmentedModelDto setDeclarations(String declarations) {
    this.declarations = declarations;
    return this;
  }

  public ModelRuleDto[] getRules() {
    return rules;
  }

  public SegmentedModelDto setRules(ModelRuleDto[] rules) {
    this.rules = rules;
    return this;
  }

  public String[] getQueries() {
    return queries;
  }

  public SegmentedModelDto setQueries(String[] queries) {
    this.queries = queries;
    return this;
  }

  @Override
  public String toString() {
    return "SegmentedModelDto{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", declarations='"
        + declarations
        + '\''
        + ", rules="
        + Arrays.toString(rules)
        + ", queries="
        + Arrays.toString(queries)
        + '}';
  }
}
