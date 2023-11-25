package com.example.locks.mapper;

import com.example.locks.entities.Movie;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.model.response.MovieResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MovieMapper {

    public Movie toEntity(MovieRequest movieRequest) {
        Movie movie = new Movie();
        movie.setMovieTitle(movieRequest.text());
        return movie;
    }

    public void mapMovieWithRequest(Movie movie, MovieRequest movieRequest) {
        movie.setMovieTitle(movieRequest.text());
    }

    public MovieResponse toResponse(Movie movie) {
        return new MovieResponse(movie.getMovieId(), movie.getMovieTitle());
    }

    public List<MovieResponse> toResponseList(List<Movie> movieList) {
        return movieList.stream().map(this::toResponse).toList();
    }
}
