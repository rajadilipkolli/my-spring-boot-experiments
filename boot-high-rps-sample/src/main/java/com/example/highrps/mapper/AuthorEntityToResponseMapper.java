package com.example.highrps.mapper;

import com.example.highrps.entities.AuthorEntity;
import com.example.highrps.model.response.AuthorResponse;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.core.convert.converter.Converter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AuthorEntityToResponseMapper extends Converter<@NonNull AuthorEntity, @NonNull AuthorResponse> {

    AuthorResponse convert(AuthorEntity authorEntity);
}
