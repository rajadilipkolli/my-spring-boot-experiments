package com.example.learning.web.controller;

import com.example.learning.model.request.PostRequest;
import com.example.learning.model.response.PostResponse;
import com.example.learning.service.PostService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
@Validated
class PostController implements PostAPI {

    private final PostService jpaPostService;
    private final PostService jooqPostService;

    PostController(
            @Qualifier("jpaPostService") PostService jpaPostService,
            @Qualifier("jooqPostService") PostService jooqPostService) {
        this.jpaPostService = jpaPostService;
        this.jooqPostService = jooqPostService;
    }

    @GetMapping("/{user_name}/posts/{title}")
    @Override
    public ResponseEntity<PostResponse> getPostByUserNameAndTitle(
            @PathVariable("user_name") String userName, @PathVariable("title") String title) {
        PostResponse postResponse = this.jooqPostService.fetchPostByUserNameAndTitle(userName, title);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{user_name}/posts/")
    @Override
    public ResponseEntity<Object> createPostByUserName(
            @RequestBody PostRequest postRequest, @PathVariable("user_name") String userName) {
        this.jpaPostService.createPost(postRequest, userName);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{title}")
                .buildAndExpand(postRequest.title())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{user_name}/posts/{title}")
    @Override
    public ResponseEntity<PostResponse> updatePostByUserName(
            @RequestBody @Valid PostRequest postRequest,
            @PathVariable("user_name") String userName,
            @PathVariable("title") String title) {
        PostResponse postResponse = this.jpaPostService.updatePostByUserNameAndId(postRequest, userName, title);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{user_name}/posts/{title}")
    @Override
    public ResponseEntity<Void> deletePostByUserName(
            @PathVariable("user_name") String userName, @PathVariable("title") String title) {
        this.jpaPostService.deletePostByIdAndUserName(userName, title);
        return ResponseEntity.accepted().build();
    }
}
