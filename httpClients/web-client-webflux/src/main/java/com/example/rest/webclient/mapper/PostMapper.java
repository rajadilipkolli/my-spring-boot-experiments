package com.example.rest.webclient.mapper;

import com.example.rest.webclient.entity.Post;
import com.example.rest.webclient.model.PostDto;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PostMapper {

    Post toEntity(PostDto postDto);
}
