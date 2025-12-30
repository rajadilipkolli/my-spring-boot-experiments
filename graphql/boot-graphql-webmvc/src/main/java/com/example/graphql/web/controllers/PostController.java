package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.exception.PostNotFoundException;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.services.PostService;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Loggable
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.findAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull PostResponse> getPostById(@PathVariable Long id) {
        return postService.findPostById(id).map(ResponseEntity::ok).orElseThrow(() -> new PostNotFoundException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@RequestBody @Validated NewPostRequest newPostRequest) {
        return postService.savePost(newPostRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull PostResponse> updatePost(
            @PathVariable Long id, @RequestBody NewPostRequest newPostRequest) {
        return postService
                .updatePost(id, newPostRequest)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePost(@PathVariable Long id) {
        if (postService.existsPostById(id)) {
            postService.deletePostById(id);
            return ResponseEntity.accepted().build();
        } else {
            throw new PostNotFoundException(id);
        }
    }
}
