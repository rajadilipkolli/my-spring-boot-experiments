package com.example.graphql.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public record AuthorResponse(
        @JsonIgnore Long id,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        String email,
        LocalDateTime registeredAt) {}
