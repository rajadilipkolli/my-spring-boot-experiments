package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.exception.PostCommentNotFoundException;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.services.PostCommentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/post/comments")
@Loggable
@Validated
public class PostCommentController {

    private final PostCommentService postCommentService;

    public PostCommentController(PostCommentService postCommentService) {
        this.postCommentService = postCommentService;
    }

    @GetMapping
    public List<PostCommentResponse> getAllPostComments() {
        return postCommentService.findAllPostComments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull PostCommentResponse> getPostCommentById(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PostCommentNotFoundException(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull PostCommentResponse> createPostComment(
            @RequestBody @Valid PostCommentRequest postCommentRequest) {
        PostCommentResponse postCommentResponse = postCommentService.addCommentToPost(postCommentRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(postCommentResponse.commentId())
                .toUri();
        return ResponseEntity.created(location).body(postCommentResponse);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull PostCommentResponse> updatePostComment(
            @PathVariable Long id, @RequestBody @Valid PostCommentRequest postCommentRequest) {
        return postCommentService
                .findCommentById(id)
                .map(postCommentObj ->
                        ResponseEntity.ok(postCommentService.updatePostComment(postCommentObj, postCommentRequest)))
                .orElseThrow(() -> new PostCommentNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostComment(@PathVariable Long id) {
        if (postCommentService.existsPostCommentById(id)) {
            postCommentService.deletePostCommentById(id);
            return ResponseEntity.accepted().build();
        } else {
            throw new PostCommentNotFoundException(id);
        }
    }
}
