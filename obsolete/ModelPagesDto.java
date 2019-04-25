package com.sri.ai.praisewm.service.dto;

import java.util.List;

public class ModelPagesDto {
  private String name;
  private List<ModelPageDto> pages;

  // no-arg constructor for JSON conversion
  public ModelPagesDto() {}

  public String getName() {
    return name;
  }

  public ModelPagesDto setName(String name) {
    this.name = name;
    return this;
  }

  public List<ModelPageDto> getPages() {
    return pages;
  }

  public ModelPagesDto setPages(List<ModelPageDto> pages) {
    this.pages = pages;
    return this;
  }

  @Override
  public String toString() {
    return "ModelPagesDto{" + "name='" + name + '\'' + ", pages=" + pages + '}';
  }
}
