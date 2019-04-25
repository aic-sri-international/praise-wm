package com.sri.ai.praisewm.web.rest.route;

import static com.sri.ai.praisewm.service.SecurityServiceImpl.getSessionId;

import com.sri.ai.praisewm.service.PraiseService;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SolverInterruptDto;
import com.sri.ai.praisewm.web.error.ProcessingException;
import com.sri.ai.praisewm.web.rest.util.SparkUtil;

/** PraiseRoutes. */
public class PraiseRoutes extends AbstractRouteGroup {
  private final PraiseService praiseService;

  public PraiseRoutes(PraiseService praiseService, spark.Service sparkService, String scope) {
    super(sparkService, scope);
    this.praiseService = praiseService;
  }

  @Override
  public void addRoutes() {
    post(
        "/buildGraph",
        (req, res) -> {
          try {
            GraphRequestDto graphRequest = SparkUtil.fromJson(req, GraphRequestDto.class);
            return SparkUtil.respondObjectOrNotFound(
                res, praiseService.buildGraph(getSessionId(req), graphRequest));
          } catch (ProcessingException pe) {
            throw pe;
          } catch (Throwable e) {
            throw new ProcessingException("Error from buildGraph call: " + e.getMessage(), e);
          }
        });

    // get segmented models
    get(
        "/segmentedModels",
        (req, res) -> SparkUtil.respondObjectOrNotFound(res, praiseService.getSegmentedModels()));

    // solve a Praise probabilistic model
    post(
        "/solve",
        (req, res) -> {
          try {
            ModelQueryDto modelQuery = SparkUtil.fromJson(req, ModelQueryDto.class);
            return SparkUtil.respondObjectOrNotFound(
                res, praiseService.solveProblem(getSessionId(req), modelQuery));
          } catch (ProcessingException pe) {
            throw pe;
          } catch (Throwable e) {
            throw new ProcessingException("Error from solve call: " + e.getMessage(), e);
          }
        });

    // interrupt a Praise solver
    post(
        "/interruptSolver",
        (req, res) -> {
          SolverInterruptDto solverInterruptDto = SparkUtil.fromJson(req, SolverInterruptDto.class);
          praiseService.interruptSolver(solverInterruptDto);
          return SparkUtil.respondNoContent(res);
        });
  }
}
