package com.sri.ai.praisewm.service.mapper;

import com.sri.ai.praisewm.db.jooq.tables.pojos.User;
import com.sri.ai.praisewm.service.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserDto userToUserDto(User user);
}
