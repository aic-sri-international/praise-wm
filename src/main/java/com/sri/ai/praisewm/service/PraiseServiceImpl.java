package com.sri.ai.praisewm.service;

import com.sri.ai.expresso.ExpressoConfiguration;
import com.sri.ai.praise.core.inference.byinputrepresentation.classbased.hogm.solver.HOGMMultiQueryProblemSolver;
import com.sri.ai.praise.other.integration.proceduralattachment.api.ProceduralAttachments;
import com.sri.ai.praisewm.service.dto.ExpressionResultDto;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.service.praise.PageModelLoader;
import com.sri.ai.praisewm.service.praise.SegmentedModelLoader;
import com.sri.ai.praisewm.service.praise.remote.ProceduralAttachmentFactory;
import com.sri.ai.praisewm.web.rest.route.PraiseRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PraiseServiceImpl implements PraiseService, Service {
  private static final Logger LOG = LoggerFactory.getLogger(PraiseServiceImpl.class);
  private SegmentedModelLoader segmentedModelLoader;
  private PageModelLoader pageModelLoader;
  private ProceduralAttachments proceduralAttachments;

  @Override
  public void start(ServiceManager serviceManager) {
    new PraiseRoutes(this, serviceManager.getRestService(), RouteScope.API);
    ExpressoConfiguration.setDisplayNumericsExactlyForSymbols(false);
    ExpressoConfiguration.setDisplayNumericsMostDecimalPlacesInApproximateRepresentationOfNumericalSymbols(3);

    pageModelLoader = new PageModelLoader();
    proceduralAttachments = new ProceduralAttachmentFactory().getAttachments();
    segmentedModelLoader =
        new SegmentedModelLoader(serviceManager.getConfiguration(), serviceManager.getEventBus());
  }

  public void stop() {
    segmentedModelLoader.stop();
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

  public List<ExpressionResultDto> solveProblem(ModelQueryDto modelQuery) {
    HOGMMultiQueryProblemSolver queryRunner =
        new HOGMMultiQueryProblemSolver(modelQuery.getModel(), modelQuery.getQuery());
    queryRunner.setProceduralAttachments(proceduralAttachments);

    List<ExpressionResultDto> results = new ArrayList<>();
    queryRunner
        .getResults()
        .forEach(
            result -> {
              List<String> answers = new ArrayList<>();
              if (result.getResult() != null) {
                answers.add(
                    queryRunner
                        .simplifyAnswer(result.getResult(), result.getQueryExpression())
                        .toString());
              }
              result.getErrors().forEach(error -> answers.add("Error: " + error.getErrorMessage()));
              results.add(
                  new ExpressionResultDto()
                      .setQuery(result.getQueryString())
                      .setAnswers(answers)
                      .setExplanationTree(result.getExplanation())
                      .setQueryDuration(result.getMillisecondsToCompute()));
            });
    return results;
  }
}
