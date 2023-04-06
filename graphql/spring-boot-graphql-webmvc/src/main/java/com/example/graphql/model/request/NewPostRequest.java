package com.example.graphql.model.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;

public record NewPostRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String email,
        boolean published,
        PostDetailsRequest details,
        List<TagsRequest> tags)
        implements Serializable {}
