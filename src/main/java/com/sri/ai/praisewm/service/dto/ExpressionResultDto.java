package com.sri.ai.praisewm.service.dto;

import com.sri.ai.util.ExplanationTree;
import java.time.Instant;
import java.util.List;

public class ExpressionResultDto {
  private String query;
  private long queryDuration;
  private Instant completionDate;
  private List<String> answers;
  private ExplanationTree explanationTree;

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

  public Instant getCompletionDate() {
    return completionDate;
  }

  public ExpressionResultDto setCompletionDate(Instant completionDate) {
    this.completionDate = completionDate;
    return this;
  }

  public List<String> getAnswers() {
    return answers;
  }

  public ExpressionResultDto setAnswers(List<String> answers) {
    this.answers = answers;
    return this;
  }

  public ExplanationTree getExplanationTree() {
    return explanationTree;
  }

  public ExpressionResultDto setExplanationTree(ExplanationTree explanationTree) {
    this.explanationTree = explanationTree;
    return this;
  }

  @Override
  public String toString() {
    return "ExpressionResultDto{" +
        "query='" + query + '\'' +
        ", queryDuration=" + queryDuration +
        ", completionDate=" + completionDate +
        ", answers=" + answers +
        ", explanationTree=" + explanationTree +
        '}';
  }
}
