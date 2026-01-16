package com.example.highrps.model.response;

import java.time.LocalDateTime;

public record PostDetailsResponse(String detailsKey, LocalDateTime createdAt, String createdBy) {}
