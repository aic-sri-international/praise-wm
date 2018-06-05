package com.sri.ai.praisewm.service;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.sri.ai.praise.application.praise.app.model.EarthquakeBurglaryAlarm;
import com.sri.ai.praise.application.praise.app.model.Election;
import com.sri.ai.praise.application.praise.app.model.ElectionAsInIJCAI2016Paper;
import com.sri.ai.praise.application.praise.app.model.ExamplePages;
import com.sri.ai.praise.application.praise.app.model.MontyHallProblem;
import com.sri.ai.praise.application.praise.app.model.Position;
import com.sri.ai.praise.inference.HOGMQueryRunner;
import com.sri.ai.praise.lang.ModelLanguage;
import com.sri.ai.praise.model.common.io.ModelPage;
import com.sri.ai.praise.model.common.io.PagedModelContainer;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ExpressionResultDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.mapper.ModelPagesMapper;
import com.sri.ai.praisewm.web.rest.route.PraiseRoutes;
import com.sri.ai.praisewm.web.rest.util.RouteScope;
import com.sri.ai.util.base.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PraiseServiceImpl implements PraiseService, Service {
  private List<ModelPagesDto> examplePages;

  @Override
  public void start(ServiceManager serviceManager) {
    new PraiseRoutes(this, serviceManager.getRestService(), RouteScope.API);
    examplePages = loadExamples();
  }

  private List<ModelPagesDto> loadExamples() {
    List<ExamplePages> examples =
        Arrays.asList(
            new EarthquakeBurglaryAlarm(),
            new MontyHallProblem(),
            new Election(),
            new ElectionAsInIJCAI2016Paper(),
            new Position());

    return examples
        .stream()
        .map(ModelPagesMapper.INSTANCE::toModelPagesDto)
        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
  }

  public List<ModelPagesDto> getExamplePages() {
    return examplePages;
  }

  @Override
  public FormattedPageModelDto toFormattedPageModel(ModelPagesDto modelPages) {
    FormattedPageModelDto formattedPagedModel = new FormattedPageModelDto();
    String basename;

      if (modelPages.getName() == null) {
        basename = "example";
      } else {
        basename = modelPages.getName()
            .replaceAll("[ \t/\\\\]", "_").toLowerCase();
      }
      formattedPagedModel.setName(basename + ".praise");

    List<Pair<String, List<String>>> pageContents =
        modelPages.getPages()
            .stream()
            .map(mp -> new Pair<>(mp.getModel(), mp.getQueries()))
            .collect(Collectors.toList());

      formattedPagedModel.setText(
          PagedModelContainer.toInternalContainerRepresentation(ModelLanguage.HOGMv1, pageContents));

      return formattedPagedModel;
  }

  @Override
  public ModelPagesDto fromFormattedPageModel(FormattedPageModelDto formattedPageModel) {
    Path path;
    try {
      path = Files.createTempFile("praise", null);
    } catch (IOException e) {
      throw new RuntimeException("Cannot create temp file", e);
    }
    try {
      Files.write(path, formattedPageModel.getText().getBytes());
    } catch (IOException e) {
      throw new RuntimeException("Cannot write to temp file", e);
    }
    List<ModelPage> mps = Collections.emptyList();
    try {
      PagedModelContainer pmc = new PagedModelContainer(formattedPageModel.getName(), path.toUri());
      mps =  pmc.getPages();
    } catch (IOException e) {
      throw new RuntimeException("Cannot create PagedModelContainer", e);
    }

    return new ModelPagesDto()
        .setName(formattedPageModel.getName())
        .setPages(ModelPagesMapper.INSTANCE.toModelPagesDto(mps));
  }

  public List<ExpressionResultDto> solveProblem(ModelQueryDto modelQuery) {
    HOGMQueryRunner queryRunner = new HOGMQueryRunner(modelQuery.getModel(), modelQuery.getQuery());

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
                      .setQueryDuration(result.getMillisecondsToCompute()));
            });
    return results;
  }
}
