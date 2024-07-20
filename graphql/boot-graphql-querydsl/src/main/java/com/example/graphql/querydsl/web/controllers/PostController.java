package com.example.graphql.querydsl.web.controllers;

import com.example.graphql.querydsl.exception.PostNotFoundException;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.services.PostService;
import com.example.graphql.querydsl.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
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
@RequestMapping("/api/posts")
class PostController {

    private final PostService postService;

    PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    PagedResult<PostResponse> getAllPosts(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindQuery findPostsQuery = new FindQuery(pageNo, pageSize, sortBy, sortDir);
        return postService.findAllPosts(findPostsQuery);
    }

    @GetMapping("/{id}")
    ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return postService.findPostById(id).map(ResponseEntity::ok).orElseThrow(() -> new PostNotFoundException(id));
    }

    @PostMapping
    ResponseEntity<PostResponse> createPost(@RequestBody @Validated CreatePostRequest createPostRequest) {
        PostResponse response = postService.savePost(createPostRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/posts/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody @Valid UpdatePostRequest postRequest) {
        return ResponseEntity.ok(postService.updatePost(id, postRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<PostResponse> deletePost(@PathVariable Long id) {
        return postService
                .findPostById(id)
                .map(post -> {
                    postService.deletePostById(id);
                    return ResponseEntity.ok(post);
                })
                .orElseThrow(() -> new PostNotFoundException(id));
    }
}
