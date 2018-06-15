package com.sri.ai.praisewm.service.dto;

public class ModelRuleDto {
  private String metadata;
  private String rule;

  public String getMetadata() {
    return metadata;
  }

  public ModelRuleDto setMetadata(String metadata) {
    this.metadata = metadata;
    return this;
  }

  public String getRule() {
    return rule;
  }

  public ModelRuleDto setRule(String rule) {
    this.rule = rule;
    return this;
  }

  @Override
  public String toString() {
    return "ModelRuleDto{" + "metadata='" + metadata + '\'' + ", rule='" + rule + '\'' + '}';
  }
}
