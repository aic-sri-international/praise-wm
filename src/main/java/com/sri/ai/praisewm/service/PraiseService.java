package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.service.dto.ExpressionResultDto;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import java.util.List;

public interface PraiseService {
  List<ModelPagesDto> getExamplePages();

  List<SegmentedModelDto> getSegmentedModels();

  List<ExpressionResultDto> solveProblem(ModelQueryDto modelQuery);

  FormattedPageModelDto toFormattedPageModel(ModelPagesDto modelPages);

  ModelPagesDto fromFormattedPageModel(FormattedPageModelDto formattedPageModel);
}
