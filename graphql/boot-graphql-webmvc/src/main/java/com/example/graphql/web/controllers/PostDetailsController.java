package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.projections.PostDetailsInfo;
import com.example.graphql.services.PostDetailsService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postdetails")
@Loggable
public class PostDetailsController {

    private final PostDetailsService postDetailsService;

    @GetMapping
    public List<PostDetailsInfo> getAllPostDetails() {
        return postDetailsService.findAllPostDetails();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailsInfo> getPostDetailsById(@PathVariable Long id) {
        return postDetailsService
                .findPostDetailsById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetailsInfo> updatePostDetails(
            @PathVariable Long id, @RequestBody PostDetailsRequest postDetailsEntity) {
        return postDetailsService
                .findDetailsById(id)
                .flatMap(postDetailsObj -> postDetailsService.updatePostDetails(postDetailsObj, postDetailsEntity))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
