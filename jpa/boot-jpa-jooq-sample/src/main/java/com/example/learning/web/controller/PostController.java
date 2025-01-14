package com.example.learning.web.controller;

import com.example.learning.model.request.PostRequest;
import com.example.learning.service.PostService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
@Validated
class PostController implements PostAPI {

    private final PostService jpaPostService;

    PostController(PostService jpaPostService) {
        this.jpaPostService = jpaPostService;
    }

    @PostMapping("/{user_name}/posts/")
    @Override
    public ResponseEntity<Object> createPostByUserName(
            @RequestBody @Valid PostRequest postRequest, @PathVariable("user_name") String userName) {
        this.jpaPostService.createPost(postRequest, userName);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{title}")
                .buildAndExpand(postRequest.title())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
