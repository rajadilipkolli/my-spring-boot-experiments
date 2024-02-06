package com.example.locks.services;

import com.example.locks.entities.Movie;
import com.example.locks.exception.MovieNotFoundException;
import com.example.locks.mapper.JpaLocksMapper;
import com.example.locks.model.query.FindMoviesQuery;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.model.response.MovieResponse;
import com.example.locks.model.response.PagedResult;
import com.example.locks.repositories.ActorRepository;
import com.example.locks.repositories.GenreRepository;
import com.example.locks.repositories.MovieRepository;
import com.example.locks.repositories.ReviewRepository;
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
    private final ActorRepository actorRepository;
    private final ReviewRepository reviewRepository;
    private final GenreRepository genreRepository;
    private final JpaLocksMapper jpaLocksMapper;

    public PagedResult<MovieResponse> findAllMovies(FindMoviesQuery findMoviesQuery) {

        // create Pageable instance
        Pageable pageable = createPageable(findMoviesQuery);

        Page<Movie> moviesPage = movieRepository.findAll(pageable);

        List<MovieResponse> movieResponseList = jpaLocksMapper.moviesListToMovieResponseList(moviesPage.getContent());

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
        return movieRepository.findById(id).map(jpaLocksMapper::movieToMovieResponse);
    }

    @Transactional
    public MovieResponse saveMovie(MovieRequest movieRequest) {
        Movie movie = jpaLocksMapper.movieRequestToMovieEntity(movieRequest);
        Movie savedMovie = movieRepository.save(movie);
        return jpaLocksMapper.movieToMovieResponse(savedMovie);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));

        // Save the updated movie object
        Movie updatedMovie = movieRepository.save(jpaLocksMapper.movieRequestToMovieWithId(movieRequest, movie.getMovieId()));

        return jpaLocksMapper.movieToMovieResponse(updatedMovie);
    }

    @Transactional
    public void deleteMovieById(Long id) {
        movieRepository.deleteById(id);
    }
}
