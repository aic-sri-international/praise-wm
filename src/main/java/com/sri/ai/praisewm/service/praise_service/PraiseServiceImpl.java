package com.sri.ai.praisewm.service.praise_service;

import com.sri.ai.expresso.ExpressoConfiguration;
import com.sri.ai.expresso.api.Expression;
import com.sri.ai.praise.PRAiSEConfiguration;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.sampling.HOGMMultiQuerySamplingProblemSolver;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.solver.HOGMProblemResult;
import com.sri.ai.praise.core.representation.interfacebased.factor.core.expressionsampling.ExpressionWithProbabilityFunction;
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
import com.sri.ai.praisewm.web.rest.route.PraiseRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PraiseServiceImpl implements PraiseService, Service {
  private static final Logger LOG = LoggerFactory.getLogger(PraiseServiceImpl.class);
  private SegmentedModelLoader segmentedModelLoader;
  private PageModelLoader pageModelLoader;
  private ProceduralAttachments proceduralAttachments;
  // Used to allow us to interrupt active solvers
  private Map<Integer, HOGMMultiQuerySamplingProblemSolver> activeSolverMap =
      Collections.synchronizedMap(new HashMap<>());
  private AtomicInteger nextSolver = new AtomicInteger();
  private QueryFunctionManager queryFunctionManager;

  @Override
  public void start(ServiceManager serviceManager) {
    new PraiseRoutes(this, serviceManager.getSparkService(), RouteScope.API);

    pageModelLoader = new PageModelLoader();
    proceduralAttachments = new ProceduralAttachmentFactory().getAttachments();
    segmentedModelLoader =
        new SegmentedModelLoader(serviceManager.getConfiguration(), serviceManager.getEventBus());
    queryFunctionManager = new QueryFunctionManager(serviceManager);
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

  public ExpressionResultDto solveProblem(String sessionId, ModelQueryDto modelQuery) {
    PRAiSEConfiguration.setUseUniformSamplingBackup(true);
    ExpressoConfiguration.setDisplayNumericsExactlyForSymbols(false);
    ExpressoConfiguration
        .setDisplayNumericsMostDecimalPlacesInApproximateRepresentationOfNumericalSymbols(3);

    LOG.info(
        "About to run HOGM Query: NumberOfDiscreteValues={}, NumberOfInitialSamples={}",
        modelQuery.getNumberOfDiscreteValues(),
        modelQuery.getNumberOfInitialSamples());

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
    try {
      // Save reference to solver in case the user makes a call to interrupt it
      // while it's processing.
      activeSolverMap.put(solverId, queryRunner);

      // Process the solver and get the results
      hogmProblemResults = queryRunner.getResults();
      if (hogmProblemResults.isEmpty()) {
        throw new RuntimeException("Solver did not return any results");
      }
    } finally {
      activeSolverMap.remove(solverId);
    }

    // There should only ever one result, if for some reason it ever returns more than
    // one, just return the first.
    final HOGMProblemResult hpResult = hogmProblemResults.get(0);
    final String queryText = hpResult.getQueryString();

    final ExpressionResultDto expressionResultDto =
        new ExpressionResultDto()
            .setQuery(queryText)
            .setQueryDuration(hpResult.getMillisecondsToCompute());

    final List<String> answers = new ArrayList<>();

    if (!hpResult.getErrors().isEmpty()) {
      hpResult.getErrors().forEach(error -> answers.add("Error: " + error.getErrorMessage()));
      return expressionResultDto.setAnswers(answers);
    }

    final Expression expression =
        Objects.requireNonNull(
            hpResult.getResult(), "Expression returned from HOGMProblemResult result is null");
    // Comment-out by request
    //  answers.add(queryRunner.simplifyAnswer(expression,
    // hpResult.getQueryExpression()).toString());
    // Note that the client requires some text in the answers array.
    answers.add("Query Completed");
    Validate.isInstanceOf(ExpressionWithProbabilityFunction.class, expression);
    ExpressionWithProbabilityFunction expressionWithProbabilityFunction =
        ((ExpressionWithProbabilityFunction) expression);

    if (expressionWithProbabilityFunction
            .getDiscretizedConditionalProbabilityDistributionFunctionQueryIndex()
        == -1) {
      answers.add(
          "The function has 0 dimensions and cannot be plotted: "
              + expressionWithProbabilityFunction.toString());
      return expressionResultDto.setAnswers(answers);
    }

    return expressionResultDto
        .setAnswers(answers)
        .setCompletionDate(Instant.now())
        .setGraphQueryResultDto(
            queryFunctionManager.processQueryResultFunction(
                sessionId, expressionWithProbabilityFunction));
  }

  @Override
  public GraphRequestResultDto buildGraph(String sessionId, GraphRequestDto graphRequestDto) {
    return queryFunctionManager.handleGraphRequest(sessionId, graphRequestDto);
  }

  @Override
  public void interruptSolver(SolverInterruptDto solverInterruptDto) {
    LOG.info("Calling interruptSolver..");
    // The client only allows one solver or subsequent buildGraph request to be
    // run at a time. @TODO The client's session id should be associated with the solver or
    // buildGraph request and this method should interrupt the one (if any) that is active.
    //
    interruptSolvers();
    LOG.info("Returned from call to interruptSolver");
  }
}
