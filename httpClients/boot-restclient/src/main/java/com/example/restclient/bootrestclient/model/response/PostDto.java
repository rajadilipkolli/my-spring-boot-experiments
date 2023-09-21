package com.example.restclient.bootrestclient.model.response;

import jakarta.validation.constraints.NotBlank;

public record PostDto(
        Long userId,
        Long id,
        @NotBlank(message = "title can't be blank") String title,
        String body) {
    public PostDto withId(Long id) {
        return new PostDto(userId(), id, title(), body());
    }
}
