package com.example.rest.proxy.web.controllers;

import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.model.response.PagedResult;
import com.example.rest.proxy.model.response.PostCommentDto;
import com.example.rest.proxy.model.response.PostResponse;
import com.example.rest.proxy.services.PostService;
import com.example.rest.proxy.utils.AppConstants;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
        return postService.findAllPosts(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return postService.findPostById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
                .build());
    }

    @GetMapping("/{id}/comments")
    ResponseEntity<List<PostCommentDto>> getPostCommentsById(@PathVariable Long id) {
        return postService.findPostCommentsById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound()
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PostResponse createPost(@RequestBody @Validated Post post) {
        return postService.savePost(post);
    }

    @PutMapping("/{id}")
    ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postService
                .findPostById(id)
                .map(postObj -> {
                    post.setId(id);
                    return ResponseEntity.ok(postService.saveAndConvertToResponse(post));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<PostResponse> deletePost(@PathVariable Long id) {
        return postService
                .findPostById(id)
                .map(post -> {
                    postService.deletePostById(id);
                    return ResponseEntity.ok(post);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
