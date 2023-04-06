package com.example.graphql.web.controllers;

import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.services.PostDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/postdetails")
@RequiredArgsConstructor
public class PostDetailsController {

    private final PostDetailsService postDetailsService;

    @GetMapping
    public List<PostDetailsEntity> getAllPostDetailss() {
        return postDetailsService.findAllPostDetailss();
    }
}
