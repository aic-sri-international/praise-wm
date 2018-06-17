package com.sri.ai.praisewm.service.mapper;

import com.sri.ai.praise.application.praise.app.model.ExamplePages;
import com.sri.ai.praise.model.common.io.ModelPage;
import com.sri.ai.praisewm.service.dto.ModelPageDto;
import com.sri.ai.praisewm.service.dto.ModelPagesDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelPagesMapper {
  ModelPagesMapper INSTANCE = Mappers.getMapper(ModelPagesMapper.class);

  @Mapping(source = "modelString", target = "model")
  @Mapping(source = "defaultQueriesToRun", target = "queries")
  ModelPageDto toModelPageDto(ModelPage modelPage);

  ModelPagesDto toModelPagesDto(ExamplePages modelPages);

  List<ModelPageDto> toModelPagesDto(List<ModelPage> modelPages);
}
