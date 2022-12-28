package com.example.rest.webclient.model;

import jakarta.validation.constraints.NotBlank;

public record Post(
        Long id,
        @NotBlank(message = "title can't be blank") String title,
        Long userId,
        String body) {

    public Post withId(Long id) {
        return new Post(id, title(), userId(), body());
    }
}
