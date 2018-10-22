package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.praisewm.service.dto.GraphQueryResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.util.graph2d.api.graph.GraphSetMaker;

public class GraphCacheEntry {
  private GraphQueryResultDto graphQueryResultDto;
  private GraphSetMaker graphSetMaker;
  private GraphRequestDto lastRequest;

  public GraphQueryResultDto getGraphQueryResultDto() {
    return graphQueryResultDto;
  }

  public GraphCacheEntry setGraphQueryResultDto(GraphQueryResultDto graphQueryResultDto) {
    this.graphQueryResultDto = graphQueryResultDto;
    return this;
  }

  public GraphSetMaker getGraphSetMaker() {
    return graphSetMaker;
  }

  public GraphCacheEntry setGraphSetMaker(GraphSetMaker graphSetMaker) {
    this.graphSetMaker = graphSetMaker;
    return this;
  }

  public GraphRequestDto getLastRequest() {
    return lastRequest;
  }

  public GraphCacheEntry setLastRequest(GraphRequestDto lastRequest) {
    this.lastRequest = lastRequest;
    return this;
  }
}
