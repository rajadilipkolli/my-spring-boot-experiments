package com.example.locks.services;

import com.example.locks.entities.Movie;
import com.example.locks.exception.MovieNotFoundException;
import com.example.locks.mapper.MovieMapper;
import com.example.locks.model.query.FindMoviesQuery;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.model.response.MovieResponse;
import com.example.locks.model.response.PagedResult;
import com.example.locks.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public PagedResult<MovieResponse> findAllMovies(FindMoviesQuery findMoviesQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findMoviesQuery);

        Page<Movie> moviesPage = movieRepository.findAll(pageable);

        List<MovieResponse> movieResponseList = movieMapper.moviesListToMovieResponseList(moviesPage.getContent());

        return new PagedResult<>(moviesPage, movieResponseList);
    }

    private Pageable createPageable(FindMoviesQuery findMoviesQuery) {
        int pageNo = Math.max(findMoviesQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findMoviesQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findMoviesQuery.sortBy())
                        : Sort.Order.desc(findMoviesQuery.sortBy()));
        return PageRequest.of(pageNo, findMoviesQuery.pageSize(), sort);
    }

    public Optional<MovieResponse> findMovieById(Long id) {
        return movieRepository.findById(id).map(movieMapper::movieToMovieResponse);
    }

    @Transactional
    public MovieResponse saveMovie(MovieRequest movieRequest) {
        Movie movie = movieMapper.movieRequestToMovie(movieRequest);
        Movie savedMovie = movieRepository.save(movie);
        return movieMapper.movieToMovieResponse(savedMovie);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));

        // Save the updated movie object
        Movie updatedMovie = movieRepository.save(movieMapper.movieRequestToMovieWithId(movieRequest, movie.getMovieId()));

        return movieMapper.movieToMovieResponse(updatedMovie);
    }

    @Transactional
    public void deleteMovieById(Long id) {
        movieRepository.deleteById(id);
    }
}
