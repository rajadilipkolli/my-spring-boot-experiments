package com.example.graphql.web.controllers;

import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.services.PostCommentService;

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

import java.util.List;

@RestController
@RequestMapping("/api/postcomments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    @GetMapping
    public List<PostCommentEntity> getAllPostComments() {
        return postCommentService.findAllPostComments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostCommentEntity> getPostCommentById(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostCommentEntity createPostComment(
            @RequestBody @Validated PostCommentEntity postCommentEntity) {
        return postCommentService.savePostComment(postCommentEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostCommentEntity> updatePostComment(
            @PathVariable Long id, @RequestBody PostCommentEntity postCommentEntity) {
        return postCommentService
                .findPostCommentById(id)
                .map(
                        postCommentObj -> {
                            postCommentEntity.setId(id);
                            return ResponseEntity.ok(
                                    postCommentService.savePostComment(postCommentEntity));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostCommentEntity> deletePostComment(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(
                        postComment -> {
                            postCommentService.deletePostCommentById(id);
                            return ResponseEntity.ok(postComment);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
