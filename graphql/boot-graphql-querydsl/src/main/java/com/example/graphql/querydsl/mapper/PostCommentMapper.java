package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = PostCommentMapperHelper.class)
public interface PostCommentMapper {

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    PostComment toEntity(CreatePostCommentRequest createPostCommentRequest);

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    void mapPostCommentWithRequest(@MappingTarget PostComment postComment, PostCommentRequest postCommentRequest);

    PostCommentResponse toResponse(PostComment postComment);

    List<PostCommentResponse> toResponseList(List<PostComment> postCommentList);
}
