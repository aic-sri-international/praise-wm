package com.sri.ai.praisewm.service.dto;

import com.sri.ai.util.explanation.tree.ExplanationTree;
import java.time.Instant;
import java.util.List;

/**
 * Represents the results of a HOGM query and the subsequent graphic representation derived from the
 * query's <code>Function</code> result.
 *
 * <p>If the actual HOGM query result contained errors, {@link #getAnswers()} will return those
 * error messages, and {@link #getGraphQueryResultDto()} will return <code>null</code>.
 *
 * <p>NOTE: {@link #getExplanationTree()} currently returns null. The <code>explanationTree</code>
 * field needs to be set from the HOGM query result when the new HOGM ExplanationTree implementation
 * is ready.
 */
public class ExpressionResultDto {
  private String query;
  private long queryDuration;
  private Instant completionDate;
  private List<String> answers;
  private ExplanationTree explanationTree;
  private GraphQueryResultDto graphQueryResultDto;

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

  public GraphQueryResultDto getGraphQueryResultDto() {
    return graphQueryResultDto;
  }

  public ExpressionResultDto setGraphQueryResultDto(GraphQueryResultDto graphQueryResultDto) {
    this.graphQueryResultDto = graphQueryResultDto;
    return this;
  }

  @Override
  public String toString() {
    return "ExpressionResultDto{"
        + "query='"
        + query
        + '\''
        + ", queryDuration="
        + queryDuration
        + ", completionDate="
        + completionDate
        + ", answers="
        + answers
        + ", explanationTree="
        + explanationTree
        + ", graphQueryResultDto="
        + graphQueryResultDto
        + '}';
  }
}
