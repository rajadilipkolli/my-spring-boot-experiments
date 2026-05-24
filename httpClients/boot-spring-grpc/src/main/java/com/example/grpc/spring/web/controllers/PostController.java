package com.example.grpc.spring.web.controllers;

import com.example.grpc.spring.model.PostDto;
import com.example.grpc.spring.services.client.PostClientService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostClientService postClientService;

    public PostController(PostClientService postClientService) {
        this.postClientService = postClientService;
    }

    @Operation(summary = "Create a new post")
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto dto) {
        return ResponseEntity.ok(postClientService.createPost(dto));
    }

    @Operation(summary = "Get a post by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postClientService.getPost(id));
    }

    @Operation(summary = "Update an existing post")
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostDto dto) {
        return ResponseEntity.ok(postClientService.updatePost(id, dto));
    }

    @Operation(summary = "Delete a post by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postClientService.deletePost(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "List all posts")
    @GetMapping
    public ResponseEntity<List<PostDto>> listPosts() {
        return ResponseEntity.ok(postClientService.listPosts());
    }
}
