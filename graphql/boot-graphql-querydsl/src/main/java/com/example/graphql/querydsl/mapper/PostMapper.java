package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.model.request.PostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import java.util.List;
import org.mapstruct.*;

@Mapper
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    Post toEntity(PostRequest postRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    void mapPostWithRequest(PostRequest postRequest, @MappingTarget Post post);

    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> postList);
}
