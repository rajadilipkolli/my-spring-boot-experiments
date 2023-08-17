package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.services.PostDetailsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postdetails")
@RequiredArgsConstructor
@Loggable
public class PostDetailsController {

    private final PostDetailsService postDetailsService;

    @GetMapping
    public List<PostDetailsEntity> getAllPostDetails() {
        return postDetailsService.findAllPostDetails();
    }
}
