package com.example.cache.services;

import com.example.cache.entities.Movie;
import com.example.cache.mapper.MovieMapper;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import com.example.cache.repositories.MovieRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final ReactiveRedisTemplate<String, Movie> reactiveRedisTemplate;

    public MovieService(
            MovieRepository movieRepository,
            MovieMapper movieMapper,
            ReactiveRedisTemplate<String, Movie> reactiveRedisTemplate) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Flux<MovieResponse> findAll() {
        return reactiveRedisTemplate
                .keys("movie:*")
                // Fetching cached movies.
                .flatMap(key -> reactiveRedisTemplate.opsForValue().get(key))
                // If cache is empty, fetch the database for movies
                .switchIfEmpty(movieRepository
                        .findAll()
                        // Persisting the fetched movies in the cache.
                        .flatMap(movie -> reactiveRedisTemplate.opsForValue().set("movie:" + movie.id(), movie))
                        // Fetching the movies from the updated cache.
                        .thenMany(reactiveRedisTemplate.keys("movie:*").flatMap(key -> reactiveRedisTemplate
                                .opsForValue()
                                .get(key))))
                .map(movieMapper::toResponse);
    }

    public Mono<MovieResponse> findMovieById(Long id) {
        return reactiveRedisTemplate
                .opsForValue()
                .get("movie:" + id)
                .switchIfEmpty(movieRepository
                        .findById(id)
                        .flatMap(movie -> reactiveRedisTemplate.opsForValue().set("movie:" + movie.id(), movie))
                        .then(reactiveRedisTemplate.opsForValue().get("movie:" + id)))
                .map(movieMapper::toResponse);
    }

    @Transactional
    public Mono<MovieResponse> saveMovie(MovieRequest movieRequest) {
        return Mono.just(movieMapper.toEntity(movieRequest))
                .flatMap(movieRepository::save)
                .flatMap(movie -> reactiveRedisTemplate
                        .opsForValue()
                        .set("movie:" + movie.id(), movie)
                        .then(reactiveRedisTemplate.opsForValue().get("movie:" + movie.id())))
                .map(movieMapper::toResponse);
    }

    @Transactional
    public Mono<MovieResponse> updateMovie(Long id, MovieRequest movieRequest) {
        return movieRepository
                .findById(id)
                .map(movie -> movieMapper.mapMovieWithRequest(movie, movieRequest))
                .flatMap(movieRepository::save)
                .flatMap(movie -> reactiveRedisTemplate
                        .opsForValue()
                        .set("movie:" + movie.id(), movie)
                        .then(reactiveRedisTemplate.opsForValue().get("movie:" + movie.id())))
                .map(movieMapper::toResponse);
    }

    @Transactional
    public Mono<Long> deleteMovieById(Long id) {
        return movieRepository.deleteById(id).then(reactiveRedisTemplate.delete("movie:" + id));
    }
}
