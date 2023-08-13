package com.example.bootr2dbc.web.controllers;

import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.model.ReactivePostRequest;
import com.example.bootr2dbc.services.ReactivePostService;
import com.example.bootr2dbc.utils.AppConstants;
import java.net.URI;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ReactivePostController {

    private final ReactivePostService reactivePostService;

    @GetMapping("/")
    public Flux<ReactivePost> getAllReactivePosts(
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return reactivePostService.findAllReactivePosts(sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ReactivePost>> getReactivePostById(@PathVariable Long id) {
        return reactivePostService
                .findReactivePostById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/")
    public Mono<ResponseEntity<ReactivePost>> createReactivePost(
            @RequestBody @Validated ReactivePostRequest reactivePostRequest,
            UriComponentsBuilder uriComponentsBuilder) {
        return reactivePostService.saveReactivePost(reactivePostRequest).map(savedPost -> {
            // Build the location URI
            String location = uriComponentsBuilder
                    .path("/api/posts/{id}")
                    .buildAndExpand(savedPost.getId())
                    .toUriString();

            // Create a ResponseEntity with the Location header and the saved ReactivePost
            return ResponseEntity.created(URI.create(location)).body(savedPost);
        });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ReactivePost>> updateReactivePost(
            @PathVariable Long id, @Validated @RequestBody ReactivePostRequest reactivePostRequest) {
        return reactivePostService
                .findReactivePostById(id)
                .flatMap(existingPost -> reactivePostService
                        .updateReactivePost(reactivePostRequest, id)
                        .map(ResponseEntity::ok))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteReactivePost(@PathVariable Long id) {
        return reactivePostService
                .findReactivePostById(id)
                .flatMap(reactivePost -> reactivePostService
                        .deleteReactivePostById(id)
                        .then(Mono.just(ResponseEntity.noContent().<Void>build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
