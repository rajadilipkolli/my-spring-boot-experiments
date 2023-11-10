package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostDetails;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.PostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.mapstruct.*;

@Mapper
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    Post toEntity(CreatePostRequest createPostRequest);

    @Mapping(target = "id", ignore = true)
    //    @Mapping(target = "createdOn", ignore = true)
    void mapPostWithRequest(PostRequest postRequest, @MappingTarget Post post);

    @Mapping(target = "createdOn", source = "details.createdOn")
    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> postList);

    @AfterMapping
    default void setAfterMappingToPost(CreatePostRequest createPostRequest, @MappingTarget Post post) {
        post.addDetails(
                new PostDetails().setCreatedBy(createPostRequest.createdBy()).setCreatedOn(LocalDateTime.now()));
    }
}
