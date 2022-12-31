package com.example.graphql.web.controllers;

import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.services.PostDetailsService;
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
@RequestMapping("/api/postdetails")
@RequiredArgsConstructor
public class PostDetailsController {

    private final PostDetailsService postDetailsService;

    @GetMapping
    public List<PostDetailsEntity> getAllPostDetailss() {
        return postDetailsService.findAllPostDetailss();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailsEntity> getPostDetailsById(@PathVariable Long id) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailsEntity createPostDetails(
            @RequestBody @Validated PostDetailsEntity postDetailsEntity) {
        return postDetailsService.savePostDetails(postDetailsEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetailsEntity> updatePostDetails(
            @PathVariable Long id, @RequestBody PostDetailsEntity postDetailsEntity) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(
                        postDetailsObj -> {
                            postDetailsEntity.setId(id);
                            return ResponseEntity.ok(
                                    postDetailsService.savePostDetails(postDetailsEntity));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostDetailsEntity> deletePostDetails(@PathVariable Long id) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(
                        postDetails -> {
                            postDetailsService.deletePostDetailsById(id);
                            return ResponseEntity.ok(postDetails);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
