package com.sri.ai.praisewm.service.dto;

import java.util.List;

public class ExpressionResultDto {
  private String query;
  private long queryDuration;
  private List<String> answers;

  // no-arg constructor for JSON conversion
  public ExpressionResultDto() {}

  public String getQuery() {
    return query;
  }

  public ExpressionResultDto setQuery(String query) {
    this.query = query;
    return this;
  }

  public long getQueryDuration() {
    return queryDuration;
  }

  public ExpressionResultDto setQueryDuration(long queryDuration) {
    this.queryDuration = queryDuration;
    return this;
  }

  public List<String> getAnswers() {
    return answers;
  }

  public ExpressionResultDto setAnswers(List<String> answers) {
    this.answers = answers;
    return this;
  }

  @Override
  public String toString() {
    return "ExpressionResultDto{"
        + "queryString='"
        + query
        + '\''
        + ", queryDuration="
        + queryDuration
        + ", answers="
        + answers
        + '}';
  }
}
