package com.sri.ai.praisewm.web.rest.route;

import static com.sri.ai.praisewm.service.SecurityServiceImpl.getSessionId;

import com.sri.ai.praisewm.service.PraiseService;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SolverInterruptDto;
import com.sri.ai.praisewm.util.FilesUtil;
import com.sri.ai.praisewm.web.error.ProcessingException;
import com.sri.ai.praisewm.web.rest.util.SparkUtil;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    // get examples
    get(
        "/examples",
        (req, res) -> SparkUtil.respondObjectOrNotFound(res, praiseService.getExamplePages()));

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

    // solve a Praise probabilistic model
    post(
        "/interruptSolver",
        (req, res) -> {
          SolverInterruptDto solverInterruptDto = SparkUtil.fromJson(req, SolverInterruptDto.class);
          praiseService.interruptSolver(solverInterruptDto);
          return SparkUtil.respondNoContent(res);
        });

    // convert to a paged model format file
    post(
        "/formatModelPages",
        (req, res) -> {
          ModelPagesDto modelPages = SparkUtil.fromJson(req, ModelPagesDto.class);
          return SparkUtil.respondObjectOrNotFound(
              res, praiseService.toFormattedPageModel(modelPages));
        });

    // convert from a paged model format file
    post(
        "/unformatModelPages",
        (req, res) -> {
          FormattedPageModelDto formattedModelPages =
              SparkUtil.fromJson(req, FormattedPageModelDto.class);
          return SparkUtil.respondObjectOrNotFound(
              res, praiseService.fromFormattedPageModel(formattedModelPages));
        });
  }

  static class Image {
    private String image;

    public Image() {
      Path imageFile = Paths.get("./SampleLineChart.png");
      this.image = FilesUtil.imageFileToBase64DataImage(imageFile);
    }

    public String getImage() {
      return image;
    }
  }
}
