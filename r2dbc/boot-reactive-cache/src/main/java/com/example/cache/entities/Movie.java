package com.example.cache.entities;

import com.example.cache.model.request.MovieRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("movies")
public record Movie(@Id Long id, String title) {

    public Movie withRequest(MovieRequest movieRequest) {
        return new Movie(id(), movieRequest.title());
    }
}
