package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.response.TagResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    Tag toEntity(TagRequest tagRequest);

    @Mapping(target = "id", ignore = true)
    void mapTagWithRequest(@MappingTarget Tag tag, TagRequest tagRequest);

    TagResponse toResponse(Tag tag);

    List<TagResponse> toResponseList(List<Tag> tagList);
}
