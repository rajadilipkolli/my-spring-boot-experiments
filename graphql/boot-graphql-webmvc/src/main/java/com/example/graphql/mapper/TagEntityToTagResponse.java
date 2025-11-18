package com.example.graphql.mapper;

import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.response.TagResponse;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface TagEntityToTagResponse extends Converter<@NonNull TagEntity, @NonNull TagResponse> {}
