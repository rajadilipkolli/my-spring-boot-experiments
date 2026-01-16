package com.example.highrps.model.request;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank(message = "TagName must not be blank") String tagName, String tagDescription) {}
