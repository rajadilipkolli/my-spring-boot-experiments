package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.exception.PostNotFoundException;
import com.example.graphql.model.query.FindQuery;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.response.PagedResult;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.services.PostService;
import com.example.graphql.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/posts")
@Loggable
@Validated
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    ResponseEntity<PagedResult<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindQuery findQuery = new FindQuery(pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(postService.findAllPosts(findQuery));
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull PostResponse> getPostById(@PathVariable Long id) {
        return postService.findPostById(id).map(ResponseEntity::ok).orElseThrow(() -> new PostNotFoundException(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull PostResponse> createPost(@RequestBody @Valid NewPostRequest newPostRequest) {
        PostResponse postResponse = postService.savePost(newPostRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(postResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(postResponse);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull PostResponse> updatePost(
            @PathVariable Long id, @RequestBody @Valid NewPostRequest newPostRequest) {
        return postService
                .updatePost(id, newPostRequest)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postService.existsPostById(id)) {
            postService.deletePostById(id);
            return ResponseEntity.accepted().build();
        } else {
            throw new PostNotFoundException(id);
        }
    }
}
