package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.entities.PostDetails;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface PostMapper {

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "id", ignore = true)
    Post toEntity(CreatePostRequest createPostRequest);

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "id", ignore = true)
    void mapPostWithRequest(UpdatePostRequest updatePostRequest, @MappingTarget Post post);

    @Mapping(target = "createdOn", source = "details.createdOn")
    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> postList);

    @AfterMapping
    default void setAfterMappingToPost(CreatePostRequest createPostRequest, @MappingTarget Post post) {
        post.addDetails(
                new PostDetails().setCreatedBy(createPostRequest.createdBy()).setCreatedOn(LocalDateTime.now()));
        createPostRequest
                .postCommentRequests()
                .forEach(postCommentRequest -> post.addComment(
                        new PostComment().setReview(postCommentRequest.review()).setCreatedOn(LocalDateTime.now())));
    }
}
