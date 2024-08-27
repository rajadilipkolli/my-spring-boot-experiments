package com.example.cache.mapper;

import com.example.cache.entities.Movie;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import org.springframework.stereotype.Service;

@Service
public class MovieMapper {

    public Movie toEntity(MovieRequest movieRequest) {
        return new Movie(null, movieRequest.title());
    }

    public Movie mapMovieWithRequest(Movie movie, MovieRequest movieRequest) {
        return movie.withRequest(movieRequest);
    }

    public MovieResponse toResponse(Movie movie) {
        return new MovieResponse(movie.id(), movie.title());
    }
}
