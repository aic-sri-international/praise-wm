package com.sri.ai.praisewm.web.rest.route;

import com.sri.ai.praisewm.service.PraiseService;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
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
    // get examples
    get(
        "/examples",
        (req, res) -> SparkUtil.respondObjectOrNotFound(res, praiseService.getExamplePages()));

    // solve a Praise probabilistic model
    post(
        "/solve",
        (req, res) -> {
          ModelQueryDto modelQuery = SparkUtil.fromJson(req, ModelQueryDto.class);
          return SparkUtil.respondObjectOrNotFound(res, praiseService.solveProblem(modelQuery));
        });

    // convert to a paged model format file
    post(
        "/formatModelPages",
        (req, res) -> {
          ModelPagesDto modelPages = SparkUtil.fromJson(req, ModelPagesDto.class);
          return SparkUtil.respondObjectOrNotFound(res, praiseService.toFormattedPageModel(modelPages));
        });

    // convert from a paged model format file
    post(
        "/unformatModelPages",
        (req, res) -> {
          FormattedPageModelDto formattedModelPages = SparkUtil.fromJson(req, FormattedPageModelDto.class);
          return SparkUtil.respondObjectOrNotFound(res, praiseService.fromFormattedPageModel(formattedModelPages));
        });
  }
}
