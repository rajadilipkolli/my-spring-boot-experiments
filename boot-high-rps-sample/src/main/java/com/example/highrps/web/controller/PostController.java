package com.example.highrps.web.controller;

import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPostByPostId(@PathVariable @Positive Long postId) {
        PostResponse postResponse = postService.findPostById(postId);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping(value = "/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid NewPostRequest newPostRequest) {
        NewPostRequest requestWithTimestamps = newPostRequest.withTimestamps(LocalDateTime.now(), null);
        PostResponse postResponse = postService.saveOrUpdatePost(requestWithTimestamps);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{postId}")
                .buildAndExpand(postResponse.postId())
                .toUri();
        return ResponseEntity.created(location).body(postResponse);
    }

    @PutMapping(value = "/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable @Positive Long postId, @RequestBody @Valid NewPostRequest newPostRequest) {
        PostResponse postResponse = postService.updatePost(postId, newPostRequest);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable @Positive Long postId) {
        postService.deletePostById(postId);
        return ResponseEntity.noContent().build();
    }
}
