package com.example.graphql.web.controllers;

import com.example.graphql.entities.PostComment;
import com.example.graphql.services.PostCommentService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/postcomments")
@Slf4j
public class PostCommentController {

    private final PostCommentService postCommentService;

    @Autowired
    public PostCommentController(PostCommentService postCommentService) {
        this.postCommentService = postCommentService;
    }

    @GetMapping
    public List<PostComment> getAllPostComments() {
        return postCommentService.findAllPostComments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostComment> getPostCommentById(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostComment createPostComment(@RequestBody @Validated PostComment postComment) {
        return postCommentService.savePostComment(postComment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostComment> updatePostComment(
            @PathVariable Long id, @RequestBody PostComment postComment) {
        return postCommentService
                .findPostCommentById(id)
                .map(
                        postCommentObj -> {
                            postComment.setId(id);
                            return ResponseEntity.ok(
                                    postCommentService.savePostComment(postComment));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostComment> deletePostComment(@PathVariable Long id) {
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
