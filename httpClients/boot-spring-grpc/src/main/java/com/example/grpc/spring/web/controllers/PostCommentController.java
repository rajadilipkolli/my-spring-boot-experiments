package com.example.grpc.spring.web.controllers;

import com.example.grpc.spring.model.PostCommentDto;
import com.example.grpc.spring.services.client.PostCommentClientService;
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
@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentController {

    private final PostCommentClientService commentClientService;

    public PostCommentController(PostCommentClientService commentClientService) {
        this.commentClientService = commentClientService;
    }

    @Operation(summary = "Add a comment to a post")
    @PostMapping
    public ResponseEntity<PostCommentDto> addComment(
            @PathVariable Long postId, @RequestBody PostCommentDto dto) {
        return ResponseEntity.ok(commentClientService.addComment(postId, dto));
    }

    @Operation(summary = "Get a comment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostCommentDto> getComment(
            @PathVariable Long postId, @PathVariable Long id) {
        return ResponseEntity.ok(commentClientService.getComment(postId, id));
    }

    @Operation(summary = "Update an existing comment")
    @PutMapping("/{id}")
    public ResponseEntity<PostCommentDto> updateComment(
            @PathVariable Long postId, @PathVariable Long id, @RequestBody PostCommentDto dto) {
        return ResponseEntity.ok(commentClientService.updateComment(postId, id, dto));
    }

    @Operation(summary = "Delete a comment by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long id) {
        if (commentClientService.deleteComment(postId, id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "List all comments for a post")
    @GetMapping
    public ResponseEntity<List<PostCommentDto>> listComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentClientService.listComments(postId));
    }
}
