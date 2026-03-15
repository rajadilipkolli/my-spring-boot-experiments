package com.example.highrps.author.query;

import com.example.highrps.author.AuthorResponse;
import com.example.highrps.entities.AuthorEntity;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.core.convert.converter.Converter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AuthorEntityToResponseMapper extends Converter<@NonNull AuthorEntity, @NonNull AuthorResponse> {

    AuthorResponse convert(AuthorEntity authorEntity);
}
