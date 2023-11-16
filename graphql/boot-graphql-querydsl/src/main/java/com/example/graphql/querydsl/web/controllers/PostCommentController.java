package com.example.graphql.querydsl.web.controllers;

import com.example.graphql.querydsl.exception.PostCommentNotFoundException;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import com.example.graphql.querydsl.services.PostCommentService;
import com.example.graphql.querydsl.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/posts/comments")
@Slf4j
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    @GetMapping
    public PagedResult<PostCommentResponse> getAllPostComments(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindQuery findPostCommentsQuery = new FindQuery(pageNo, pageSize, sortBy, sortDir);
        return postCommentService.findAllPostComments(findPostCommentsQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostCommentResponse> getPostCommentById(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PostCommentNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<PostCommentResponse> createPostComment(
            @RequestBody @Validated CreatePostCommentRequest createPostCommentRequest) {
        PostCommentResponse response = postCommentService.savePostComment(createPostCommentRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/posts/comments/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostCommentResponse> updatePostComment(
            @PathVariable Long id, @RequestBody @Valid PostCommentRequest postCommentRequest) {
        return ResponseEntity.ok(postCommentService.updatePostComment(id, postCommentRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostCommentResponse> deletePostComment(@PathVariable Long id) {
        return postCommentService
                .findPostCommentById(id)
                .map(postComment -> {
                    postCommentService.deletePostCommentById(id);
                    return ResponseEntity.ok(postComment);
                })
                .orElseThrow(() -> new PostCommentNotFoundException(id));
    }
}
