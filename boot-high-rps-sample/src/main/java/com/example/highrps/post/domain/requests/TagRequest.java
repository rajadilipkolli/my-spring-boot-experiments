package com.example.highrps.post.domain.requests;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank(message = "TagName must not be blank") String tagName, String tagDescription) {}
