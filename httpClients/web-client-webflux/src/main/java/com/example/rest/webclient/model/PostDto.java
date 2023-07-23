package com.example.rest.webclient.model;

import jakarta.validation.constraints.NotBlank;

public record PostDto(
        Long id,
        @NotBlank(message = "Title can't be blank") String title,
        Long userId,
        String body) {

    public PostDto withId(Long id) {
        return new PostDto(id, title(), userId(), body());
    }
}
