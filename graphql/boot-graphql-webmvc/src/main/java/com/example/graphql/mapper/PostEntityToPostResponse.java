package com.example.graphql.mapper;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.entities.PostTagEntity;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.model.response.TagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface PostEntityToPostResponse extends Converter<PostEntity, PostResponse> {

    PostResponse convert(PostEntity postEntity);

    @Mapping(target = "tagName", source = "tagEntity.tagName")
    TagResponse postTagEntityToTagResponse(PostTagEntity postTagEntity);
}
