package com.example.rest.webclient.mapper;

import com.example.rest.webclient.entity.Post;
import com.example.rest.webclient.model.PostDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PostMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "body", source = "body")
    Post toEntity(PostDto postDto);
}
