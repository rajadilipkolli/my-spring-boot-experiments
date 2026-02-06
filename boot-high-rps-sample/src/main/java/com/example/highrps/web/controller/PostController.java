package com.example.highrps.web.controller;

import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.model.response.PostResponse;
import com.example.highrps.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @GetMapping("/posts/{title}/{email}")
    public ResponseEntity<PostResponse> getPostByTitleAndEmail(
            @PathVariable @NotBlank String title, @NotBlank @PathVariable String email) {
        PostResponse postResponse = postService.findPostByEmailAndTitle(email, title);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping(value = "/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid NewPostRequest newPostRequest) {
        NewPostRequest withCreatedAt = newPostRequest.withCreatedAt(LocalDateTime.now());
        PostResponse postResponse = postService.saveOrUpdatePost(withCreatedAt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{title}/{email}")
                .buildAndExpand(postResponse.title(), withCreatedAt.email())
                .toUri();
        return ResponseEntity.created(location).body(postResponse);
    }

    @PutMapping(value = "/posts/{title}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String title, @RequestBody @Valid NewPostRequest newPostRequest) {
        PostResponse postResponse = postService.updatePost(title, newPostRequest);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/posts/{title}/{email}")
    public ResponseEntity<Void> deletePost(@PathVariable @NotBlank String title, @PathVariable @NotBlank String email) {
        postService.deletePost(title, email);
        return ResponseEntity.noContent().build();
    }
}
