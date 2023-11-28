package com.example.locks.mapper;

import com.example.locks.entities.Movie;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.model.response.MovieResponse;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface MovieMapper {

    Movie movieRequestToMovie(MovieRequest movieRequest);

    default Movie movieRequestToMovieWithId(MovieRequest movieRequest, Long movieId) {
        var movie = movieRequestToMovie(movieRequest);
        movie.setMovieId(movieId);
        return movie;
    }

    MovieRequest MovieToMovieRequest(Movie movie);

    MovieResponse movieToMovieResponse(Movie movie);

    default List<MovieResponse> moviesListToMovieResponseList(List<Movie> moviesList) {
        return moviesList.stream().map(this::movieToMovieResponse).collect(Collectors.toList());
    }

}
