package com.example.rest.webclient.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;

public record PostDto(
        @Id Long id,
        @NotBlank(message = "title can't be blank") String title,
        Long userId,
        String body) {

    public PostDto withId(Long id) {
        return new PostDto(id, title(), userId(), body());
    }
}
