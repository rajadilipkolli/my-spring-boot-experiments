package com.example.rest.webclient.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;

public record Post(
        @Id Long id,
        @NotBlank(message = "title can't be blank") String title,
        Long userId,
        String body) {

    public Post withId(Long id) {
        return new Post(id, title(), userId(), body());
    }
}
