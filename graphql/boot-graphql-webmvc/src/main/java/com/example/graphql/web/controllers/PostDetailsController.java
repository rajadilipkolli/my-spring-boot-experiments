package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PostDetailsResponse;
import com.example.graphql.services.PostDetailsService;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post/details")
@Loggable
public class PostDetailsController {

    private final PostDetailsService postDetailsService;

    public PostDetailsController(PostDetailsService postDetailsService) {
        this.postDetailsService = postDetailsService;
    }

    @GetMapping
    public List<PostDetailsResponse> getAllPostDetails() {
        return postDetailsService.findAllPostDetails();
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull PostDetailsResponse> getPostDetailsById(@PathVariable Long id) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull PostDetailsResponse> updatePostDetails(
            @PathVariable Long id, @RequestBody PostDetailsRequest postDetailsEntity) {
        return postDetailsService
                .findDetailsById(id)
                .flatMap(postDetailsObj -> postDetailsService.updatePostDetails(postDetailsObj, postDetailsEntity))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
