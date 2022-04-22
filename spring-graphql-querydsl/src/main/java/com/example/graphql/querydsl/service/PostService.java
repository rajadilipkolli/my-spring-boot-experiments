package com.example.graphql.querydsl.service;

import com.example.graphql.querydsl.model.request.AddTagRequestDTO;
import com.example.graphql.querydsl.model.request.PostRequestDTO;
import com.example.graphql.querydsl.model.response.PostResponse;

import java.util.List;

public interface PostService {
    PostResponse createPost(PostRequestDTO postRequestDTO);

    PostResponse addTagsToPost(AddTagRequestDTO addTagRequestDTO);

    Long totalPosts();

    List<PostResponse> getPostsByUserName(String name);
}
