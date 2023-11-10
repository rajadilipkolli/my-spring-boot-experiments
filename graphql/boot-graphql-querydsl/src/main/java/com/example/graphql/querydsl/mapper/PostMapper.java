package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.*;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.model.response.TagResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
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
    @Mapping(target = "postCommentResponses", source = "comments")
    @Mapping(target = "tagResponses", source = "tags")
    PostResponse toResponse(Post post);

    @Mapping(target = "id", source = "tag.id")
    @Mapping(target = "name", source = "tag.name")
    TagResponse postTagToTagResponse(PostTag tag);

    List<PostResponse> toResponseList(List<Post> postList);

    @AfterMapping
    default void setAfterMappingToPost(CreatePostRequest createPostRequest, @MappingTarget Post post) {
        post.addDetails(
                new PostDetails().setCreatedBy(createPostRequest.createdBy()).setCreatedOn(LocalDateTime.now()));
        createPostRequest
                .postCommentRequests()
                .forEach(postCommentRequest -> post.addComment(
                        new PostComment().setReview(postCommentRequest.review()).setCreatedOn(LocalDateTime.now())));
        createPostRequest.tagRequests().forEach(tagRequest -> post.addTag(new Tag().setName(tagRequest.name())));
    }
}
