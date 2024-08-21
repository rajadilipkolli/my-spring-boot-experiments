package com.example.cache.services;

import com.example.cache.entities.Movie;
import com.example.cache.exception.MovieNotFoundException;
import com.example.cache.mapper.MovieMapper;
import com.example.cache.model.query.FindMoviesQuery;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import com.example.cache.model.response.PagedResult;
import com.example.cache.repositories.MovieRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<MovieResponse> movieResponseList = movieMapper.toResponseList(moviesPage.getContent());

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
        return movieRepository.findById(id).map(movieMapper::toResponse);
    }

    @Transactional
    public MovieResponse saveMovie(MovieRequest movieRequest) {
        Movie movie = movieMapper.toEntity(movieRequest);
        Movie savedMovie = movieRepository.save(movie);
        return movieMapper.toResponse(savedMovie);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));

        // Update the movie object with data from movieRequest
        movieMapper.mapMovieWithRequest(movie, movieRequest);

        // Save the updated movie object
        Movie updatedMovie = movieRepository.save(movie);

        return movieMapper.toResponse(updatedMovie);
    }

    @Transactional
    public void deleteMovieById(Long id) {
        movieRepository.deleteById(id);
    }
}
