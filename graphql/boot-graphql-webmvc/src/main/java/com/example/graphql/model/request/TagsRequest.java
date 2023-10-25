package com.example.graphql.model.request;

import jakarta.validation.constraints.NotBlank;

public record TagsRequest(
        @NotBlank(message = "TagName must not be blank") String tagName, String tagDescription) {}
