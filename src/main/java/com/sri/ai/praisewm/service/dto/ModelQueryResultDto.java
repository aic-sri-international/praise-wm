package com.sri.ai.praisewm.service.dto;

import java.util.ArrayList;
import java.util.List;

public class ModelQueryResultDto {
  private String queryString = "";
  private long queryDuration = -1;
  private List<String> answers = new ArrayList<>();

  // no-arg constructor for JSON conversion
  public ModelQueryResultDto() {}

  public String getQueryString() {
    return queryString;
  }

  public ModelQueryResultDto setQueryString(String queryString) {
    this.queryString = queryString;
    return this;
  }

  public long getQueryDuration() {
    return queryDuration;
  }

  public ModelQueryResultDto setQueryDuration(long queryDuration) {
    this.queryDuration = queryDuration;
    return this;
  }

  public List<String> getAnswers() {
    return answers;
  }

  public ModelQueryResultDto setAnswers(List<String> answers) {
    this.answers = answers;
    return this;
  }
}
