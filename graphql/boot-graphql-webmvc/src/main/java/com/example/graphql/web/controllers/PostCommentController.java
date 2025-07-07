package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.services.PostCommentService;
import java.util.List;
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
@Loggable
public class PostCommentController {

    private final PostCommentService postCommentService;

    @GetMapping
    public List<PostCommentResponse> getAllPostComments() {
        return postCommentService.findAllPostComments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostCommentResponse> getPostCommentById(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostCommentResponse createPostComment(@RequestBody @Validated PostCommentRequest postCommentRequest) {
        return postCommentService.addCommentToPost(postCommentRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostCommentResponse> updatePostComment(
            @PathVariable Long id, @RequestBody PostCommentRequest postCommentRequest) {
        return postCommentService
                .findCommentById(id)
                .map(postCommentObj ->
                        ResponseEntity.ok(postCommentService.updatePostComment(postCommentObj, postCommentRequest)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePostComment(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(postComment -> {
                    postCommentService.deletePostCommentById(id);
                    return ResponseEntity.accepted().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
