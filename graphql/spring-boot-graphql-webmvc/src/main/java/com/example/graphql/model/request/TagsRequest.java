package com.example.graphql.model.request;

import jakarta.validation.constraints.NotBlank;

public record TagsRequest(@NotBlank String tagName, String tagDescription) {}
