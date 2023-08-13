package com.example.bootr2dbc.web.controllers;

import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.model.response.PagedResult;
import com.example.bootr2dbc.services.ReactivePostService;
import com.example.bootr2dbc.utils.AppConstants;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Slf4j
public class ReactivePostController {

    private final ReactivePostService reactivePostService;

    @Autowired
    public ReactivePostController(ReactivePostService reactivePostService) {
        this.reactivePostService = reactivePostService;
    }

    @GetMapping
    public PagedResult<ReactivePost> getAllReactivePosts(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return reactivePostService.findAllReactivePosts(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReactivePost> getReactivePostById(@PathVariable Long id) {
        return reactivePostService
                .findReactivePostById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReactivePost createReactivePost(@RequestBody @Validated ReactivePost reactivePost) {
        return reactivePostService.saveReactivePost(reactivePost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReactivePost> updateReactivePost(
            @PathVariable Long id, @RequestBody ReactivePost reactivePost) {
        return reactivePostService
                .findReactivePostById(id)
                .map(reactivePostObj -> {
                    reactivePost.setId(id);
                    return ResponseEntity.ok(reactivePostService.saveReactivePost(reactivePost));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReactivePost> deleteReactivePost(@PathVariable Long id) {
        return reactivePostService
                .findReactivePostById(id)
                .map(reactivePost -> {
                    reactivePostService.deleteReactivePostById(id);
                    return ResponseEntity.ok(reactivePost);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
