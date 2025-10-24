package com.example.graphql.mapper;

import com.example.graphql.model.response.PostDetailsResponse;
import com.example.graphql.projections.PostDetailsInfo;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface PostDetailInfoToResponseMapper
        extends Converter<@NonNull PostDetailsInfo, @NonNull PostDetailsResponse> {}
