package com.example.graphql.model.request;

import jakarta.validation.constraints.NotBlank;

public record PostDetailsRequest(
        @NotBlank(message = "Key must not be blank") String detailsKey, String createdBy) {}
