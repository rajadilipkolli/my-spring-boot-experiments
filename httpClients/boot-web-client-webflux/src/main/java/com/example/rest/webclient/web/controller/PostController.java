package com.example.rest.webclient.web.controller;

import com.example.rest.webclient.model.PostDto;
import com.example.rest.webclient.service.PostService;
import com.example.rest.webclient.utils.AppConstants;
import jakarta.validation.Valid;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Flux<PostDto> getAllPosts(
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return postService.findAllPosts(sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PostDto>> getPostById(@PathVariable Long id) {
        return postService
                .findPostById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PostDto> createPost(@RequestBody @Valid PostDto post) {
        return postService.savePost(post);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Mono<PostDto>>> updatePost(
            @PathVariable Long id, @RequestBody PostDto post) {
        return postService
                .findPostById(id)
                .map(postObj -> ResponseEntity.ok(postService.savePost(post.withId(id))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<PostDto>> deletePost(@PathVariable Long id) {
        return postService
                .findPostById(id)
                .map(
                        post -> {
                            postService.deletePostById(id);
                            return ResponseEntity.ok(post);
                        })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
