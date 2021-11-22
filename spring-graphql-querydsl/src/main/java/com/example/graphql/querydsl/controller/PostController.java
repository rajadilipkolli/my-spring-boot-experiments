package com.example.graphql.querydsl.controller;

import com.example.graphql.querydsl.model.request.AddTagRequestDTO;
import com.example.graphql.querydsl.model.request.PostRequestDTO;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @QueryMapping
  public Long countPosts(){
    return this.postService.totalPosts();
  }

  @MutationMapping
  public PostResponse createPost(@Argument("postRequestDTO") PostRequestDTO postRequestDTO) {
    return this.postService.createPost(postRequestDTO);
  }

  @MutationMapping
  public PostResponse addTagsToPost(@Argument("addTagRequest") AddTagRequestDTO addTagRequest) {
    return this.postService.addTagsToPost(addTagRequest);
  }

}
