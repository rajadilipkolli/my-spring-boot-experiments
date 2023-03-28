package com.example.graphql.model.response;

import java.time.LocalDateTime;

public record PostDetailsResponse(String detailsKey, LocalDateTime createdAt, String createdBy) {}
