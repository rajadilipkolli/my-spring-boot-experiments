package com.example.graphql.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AuthorResponse(
        @JsonIgnore Long id,
        String firstName,
        String middleName,
        String lastName,
        String mobile,
        String email,
        LocalDateTime registeredAt) {}
