package com.example.highrps.mapper;

import com.example.highrps.entities.PostEntity;
import com.example.highrps.entities.PostTagEntity;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.model.response.TagResponse;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.core.convert.converter.Converter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PostEntityToPostResponse extends Converter<@NonNull PostEntity, @NonNull PostResponse> {

    PostResponse convert(PostEntity postEntity);

    @Mapping(target = "tagName", source = "tagEntity.tagName")
    @Mapping(target = "tagDescription", source = "tagEntity.tagDescription")
    @Mapping(target = "id", source = "tagEntity.id")
    TagResponse postTagEntityToTagResponse(PostTagEntity postTagEntity);
}
