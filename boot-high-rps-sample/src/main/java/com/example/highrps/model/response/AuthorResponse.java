package com.example.highrps.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import tools.jackson.databind.json.JsonMapper;

public record AuthorResponse(
        @JsonIgnore Long id,
        String firstName,
        String middleName,
        String lastName,
        Long mobile,
        String email,
        LocalDateTime registeredAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt) {
    private static final JsonMapper JSON = new JsonMapper();

    public static AuthorResponse fromJson(String cached) {
        return JSON.readValue(cached, AuthorResponse.class);
    }

    public static String toJson(AuthorResponse authorResponse) {
        return JSON.writeValueAsString(authorResponse);
    }
}
