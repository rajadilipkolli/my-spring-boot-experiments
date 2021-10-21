package com.example.graphql.querydsl.controller;

import com.example.graphql.querydsl.model.request.PostRequestDTO;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.service.PostService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @MutationMapping
  public PostResponse createPost(@Argument("postRequestDTO") PostRequestDTO postRequestDTO) {
    return this.postService.createPost(postRequestDTO);
  }
}
