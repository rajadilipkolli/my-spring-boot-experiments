package com.example.graphql.web.controllers;

import com.example.graphql.entities.PostDetails;
import com.example.graphql.services.PostDetailsService;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postdetails")
@Slf4j
public class PostDetailsController {

    private final PostDetailsService postDetailsService;

    @Autowired
    public PostDetailsController(PostDetailsService postDetailsService) {
        this.postDetailsService = postDetailsService;
    }

    @GetMapping
    public List<PostDetails> getAllPostDetailss() {
        return postDetailsService.findAllPostDetailss();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetails> getPostDetailsById(@PathVariable Long id) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetails createPostDetails(@RequestBody @Validated PostDetails postDetails) {
        return postDetailsService.savePostDetails(postDetails);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetails> updatePostDetails(
            @PathVariable Long id, @RequestBody PostDetails postDetails) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(
                        postDetailsObj -> {
                            postDetails.setId(id);
                            return ResponseEntity.ok(
                                    postDetailsService.savePostDetails(postDetails));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PostDetails> deletePostDetails(@PathVariable Long id) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(
                        postDetails -> {
                            postDetailsService.deletePostDetailsById(id);
                            return ResponseEntity.ok(postDetails);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
