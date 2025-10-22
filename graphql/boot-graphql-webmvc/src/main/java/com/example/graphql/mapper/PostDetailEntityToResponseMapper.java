package com.example.graphql.mapper;

import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.model.response.PostDetailsResponse;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface PostDetailEntityToResponseMapper
        extends Converter<@NonNull PostDetailsEntity, @NonNull PostDetailsResponse> {}
