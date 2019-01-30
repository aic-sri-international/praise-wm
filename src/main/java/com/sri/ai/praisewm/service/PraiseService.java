package com.sri.ai.praisewm.service;

import com.sri.ai.praisewm.service.dto.ExpressionResultDto;
import com.sri.ai.praisewm.service.dto.FormattedPageModelDto;
import com.sri.ai.praisewm.service.dto.GraphRequestDto;
import com.sri.ai.praisewm.service.dto.GraphRequestResultDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import com.sri.ai.praisewm.service.dto.ModelQueryDto;
import com.sri.ai.praisewm.service.dto.SegmentedModelDto;
import com.sri.ai.praisewm.service.dto.SolverInterruptDto;
import java.util.List;

public interface PraiseService {
  List<ModelPagesDto> getExamplePages();

  List<SegmentedModelDto> getSegmentedModels();

  ExpressionResultDto solveProblem(String sessionId, ModelQueryDto modelQuery);

  FormattedPageModelDto toFormattedPageModel(ModelPagesDto modelPages);

  ModelPagesDto fromFormattedPageModel(FormattedPageModelDto formattedPageModel);

  void interruptSolver(SolverInterruptDto solverInterruptDto);

  GraphRequestResultDto buildGraph(String sessionId, GraphRequestDto graphRequestDto);
}
