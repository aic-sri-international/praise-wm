package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.expresso.ExpressoConfiguration;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.sampling.HOGMMultiQuerySamplingProblemSolver;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.solver.HOGMProblemResult;
import com.sri.ai.praise.other.integration.proceduralattachment.api.ProceduralAttachments;
import com.sri.ai.praisewm.service.PraiseService;
import com.sri.ai.praisewm.service.Service;
import com.sri.ai.praisewm.service.ServiceManager;
import com.sri.ai.praisewm.service.dto.ExpressionResultDto;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.service.dto.SolverInterruptDto;
import com.sri.ai.praisewm.service.praise.PageModelLoader;
import com.sri.ai.praisewm.service.praise.SegmentedModelLoader;
import com.sri.ai.praisewm.service.praise.remote.ProceduralAttachmentFactory;
import com.sri.ai.praisewm.web.error.ProcessingException;
import com.sri.ai.praisewm.web.rest.route.PraiseRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PraiseServiceImpl implements PraiseService, Service {
  private static final Logger LOG = LoggerFactory.getLogger(PraiseServiceImpl.class);
  private SegmentedModelLoader segmentedModelLoader;
  private PageModelLoader pageModelLoader;
  private ProceduralAttachments proceduralAttachments;
  private Map<Integer, HOGMMultiQuerySamplingProblemSolver> activeSolverMap =
      Collections.synchronizedMap(new HashMap<>());
  private AtomicInteger nextSolver = new AtomicInteger();
  private GraphManager graphManager;

  @Override
  public void start(ServiceManager serviceManager) {
    new PraiseRoutes(this, serviceManager.getRestService(), RouteScope.API);

    pageModelLoader = new PageModelLoader();
    proceduralAttachments = new ProceduralAttachmentFactory().getAttachments();
    segmentedModelLoader =
        new SegmentedModelLoader(serviceManager.getConfiguration(), serviceManager.getEventBus());
    graphManager = new GraphManager(serviceManager);
  }

  public void stop() {
    segmentedModelLoader.stop();
    interruptSolvers();
  }

  private void interruptSolvers() {
    for (HOGMMultiQuerySamplingProblemSolver solver : activeSolverMap.values()) {
      try {
        solver.interrupt();
      } catch (Throwable e) {
        LOG.warn("Error trying to stop HOGMMultiQuerySamplingProblemSolver", e);
      }
    }
  }

  public List<ModelPagesDto> getExamplePages() {
    return pageModelLoader.getExamplePages();
  }

  @Override
  public List<SegmentedModelDto> getSegmentedModels() {
    return segmentedModelLoader.getSegmentedModels();
  }

  @Override
  public FormattedPageModelDto toFormattedPageModel(ModelPagesDto modelPages) {
    return PageModelLoader.toFormattedPageModel(modelPages);
  }

  @Override
  public ModelPagesDto fromFormattedPageModel(FormattedPageModelDto formattedPageModel) {
    return PageModelLoader.fromFormattedPageModel(formattedPageModel);
  }

  public List<ExpressionResultDto> solveProblem(String sessionId, ModelQueryDto modelQuery) {
        ExpressoConfiguration.setDisplayNumericsExactlyForSymbols(false);
        ExpressoConfiguration
            .setDisplayNumericsMostDecimalPlacesInApproximateRepresentationOfNumericalSymbols(3);

    HOGMMultiQuerySamplingProblemSolver queryRunner =
        new HOGMMultiQuerySamplingProblemSolver(
            modelQuery.getModel(),
            Collections.singletonList(modelQuery.getQuery()),
            v -> modelQuery.getNumberOfDiscreteValues(),
            modelQuery.getNumberOfInitialSamples(),
            new Random());

    queryRunner.setProceduralAttachments(proceduralAttachments);

    List<? extends HOGMProblemResult> hogmProblemResults;

    int solverId = nextSolver.incrementAndGet();
    activeSolverMap.put(solverId, queryRunner);

    try {
      hogmProblemResults = queryRunner.getResults();
      if (hogmProblemResults.isEmpty()) {
        throw new RuntimeException("Solver did not return any results");
      }
    } catch (Exception e) {
      throw new ProcessingException("Cannot solve query: " + e.getMessage(), e);
    } finally {
      activeSolverMap.remove(solverId);
    }

    try {
      List<ExpressionResultDto> results = new ArrayList<>();
      List<String> answers = new ArrayList<>();
      HOGMProblemResult hpResult = hogmProblemResults.get(0);
      Expression result = hpResult.getResult();
      if (result != null) {
        answers.add(queryRunner.simplifyAnswer(result, hpResult.getQueryExpression()).toString());
      }

      hpResult.getErrors().forEach(error -> answers.add("Error: " + error.getErrorMessage()));

      results.add(
          new ExpressionResultDto()
              .setQuery(hpResult.getQueryString())
              .setAnswers(answers)
              .setExplanationTree(hpResult.getExplanation())
              .setQueryDuration(hpResult.getMillisecondsToCompute())
              .setCompletionDate(Instant.now())
              .setGraphQueryResultDto(graphManager.setGraphQueryResult(sessionId, result)));
      return results;
    } catch (Exception e) {
      throw new ProcessingException("Cannot solve query: " + e.getMessage(), e);
    }
  }

  @Override
  public GraphRequestResultDto buildGraph(String sessionId, GraphRequestDto graphRequestDto) {
    return graphManager.handleGraphRequest(sessionId, graphRequestDto);
  }

  @Override
  public void interruptSolver(SolverInterruptDto solverInterruptDto) {
    LOG.info("Calling interruptSolver..");
    interruptSolvers();
    LOG.info("Returned from call to interruptSolver");
  }
}
