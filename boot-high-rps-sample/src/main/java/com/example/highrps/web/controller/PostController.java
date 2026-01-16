package com.example.highrps.web.controller;

import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.service.PostService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{title}")
    public ResponseEntity<PostResponse> getPostByTitle(@PathVariable String title) {
        PostResponse postResponse = postService.findPostByTitle(title);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping(value = "/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid NewPostRequest newPostRequest) {
        PostResponse postResponse = postService.savePost(newPostRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{title}")
                .buildAndExpand(postResponse.title())
                .toUri();
        return ResponseEntity.created(location).body(postResponse);
    }

    @PutMapping(value = "/posts/{title}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String title, @RequestBody @Valid NewPostRequest newPostRequest) {
        if (!title.equals(newPostRequest.title())) {
            return ResponseEntity.badRequest().build();
        }
        PostResponse postResponse = postService.updatePost(newPostRequest);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/posts/{title}")
    public ResponseEntity<Void> deletePost(@PathVariable String title) {
        postService.deletePost(title);
        return ResponseEntity.noContent().build();
    }
}
