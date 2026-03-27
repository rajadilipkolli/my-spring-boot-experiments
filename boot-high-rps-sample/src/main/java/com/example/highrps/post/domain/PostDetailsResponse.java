package com.example.highrps.post.domain;

import java.time.LocalDateTime;

public record PostDetailsResponse(String detailsKey, LocalDateTime createdAt, String createdBy) {}
