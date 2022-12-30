package com.example.graphql.web.controllers;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.services.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<PostEntity> getAllPosts() {
        return postService.findAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostEntity> getPostById(@PathVariable Long id) {
        return postService
                .findPostById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostEntity createPost(@RequestBody @Validated PostEntity post) {
        return postService.savePost(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostEntity> updatePost(
            @PathVariable Long id, @RequestBody PostEntity post) {
        return postService
                .findPostById(id)
                .map(
                        postObj -> {
                            post.setId(id);
                            return ResponseEntity.ok(postService.savePost(post));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostEntity> deletePost(@PathVariable Long id) {
        return postService
                .findPostById(id)
                .map(
                        post -> {
                            postService.deletePostById(id);
                            return ResponseEntity.ok(post);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
