package com.sri.ai.praisewm.service.dto;

public class FormattedPageModelDto {
  private String name;
  private String text;

  public String getName() {
    return name;
  }

  public FormattedPageModelDto setName(String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public FormattedPageModelDto setText(String text) {
    this.text = text;
    return this;
  }

  @Override
  public String toString() {
    return "FormattedPageModelDto{" + "name='" + name + '\'' + ", text='" + text + '\'' + '}';
  }
}
