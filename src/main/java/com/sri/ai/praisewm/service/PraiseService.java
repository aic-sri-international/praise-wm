package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.service.dto.ExpressionResultDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.service.dto.SolverInterruptDto;
import java.util.List;

/**
 * The PraiseService manages aic-praise model queries.
 */
public interface PraiseService {

  /**
   * Get a list of all models managed by this service.
   *
   * @return the list of models
   */
  List<SegmentedModelDto> getSegmentedModels();

  /**
   * Solve a Praise HOGM query.
   * <p>
   * The result from a successful HOGM query contains a <code>Function</code> object that
   * is used to obtain a graphic or map data representation of a problem result.
   * <p>
   * The <code>Function</code> object is accessed to obtain the variables/values from the
   * model and to determine which variable to select as the initial x-axis variable. This
   * information, along with the <code>Function</code> object, are passed to an instance of
   * <code>com.sri.ai.util.graph2d.core.DefaultGraphSetMaker</code> to obtain a region to
   * value map, a bar graph, or a line chart.
   *
   * @param sessionId the client's sessionId
   * @param modelQuery the data required for a HOGM query and query options.
   * @return the result of the query
   */
  ExpressionResultDto solveProblem(String sessionId, ModelQueryDto modelQuery);

  /**
   * Interrupt a running query.
   *
   * @param solverInterruptDto identifying data
   */
  void interruptSolver(SolverInterruptDto solverInterruptDto);

  /**
   * Get data that represents the result of a query <code>Function</code>.
   *
   * @param sessionId the client's sessionId
   * @param graphRequestDto function query criteria
   * @return a graphic representation result
   */
  GraphRequestResultDto buildGraph(String sessionId, GraphRequestDto graphRequestDto);
}
