package com.example.rest.webclient.model;

public record Post(Long id, String title, Long userId, String body) {

    public Post withId(Long id) {
        return new Post(id, title(), userId(), body());
    }
}
