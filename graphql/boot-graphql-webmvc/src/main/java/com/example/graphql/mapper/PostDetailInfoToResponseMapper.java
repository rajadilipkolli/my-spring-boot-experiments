package com.example.graphql.mapper;

import com.example.graphql.model.response.PostDetailsResponse;
import com.example.graphql.projections.PostDetailsInfo;
import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface PostDetailInfoToResponseMapper extends Converter<PostDetailsInfo, PostDetailsResponse> {

    // Map PostDetailsInfo projection to PostDetailsResponse record
    PostDetailsResponse covert(PostDetailsInfo info);
}
