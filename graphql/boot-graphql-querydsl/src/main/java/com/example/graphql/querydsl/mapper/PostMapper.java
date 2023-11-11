package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.*;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.model.response.TagResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = PostMapperHelper.class)
public interface PostMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "id", ignore = true)
    Post toEntity(CreatePostRequest createPostRequest);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "id", ignore = true)
    void mapPostWithRequest(UpdatePostRequest updatePostRequest, @MappingTarget Post post);

    @Mapping(target = "createdOn", source = "details.createdOn")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "tags", source = "tags")
    PostResponse toResponse(Post post);

    @Mapping(target = "id", source = "tag.id")
    @Mapping(target = "name", source = "tag.name")
    TagResponse postTagToTagResponse(PostTag tag);

    List<PostResponse> toResponseList(List<Post> postList);

    @Mapping(target = "tags", ignore = true)
    Post setTags(List<TagRequest> tagRequests, Post post);
}
