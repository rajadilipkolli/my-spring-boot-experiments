package com.example.cache.services;

import com.example.cache.entities.Movie;
import com.example.cache.mapper.MovieMapper;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import com.example.cache.repositories.MovieRepository;
import com.example.cache.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final ReactiveRedisTemplate<String, Movie> reactiveRedisTemplate;
    private final ReactiveValueOperations<String, Movie> stringMovieReactiveValueOperations;

    @Autowired
    public MovieService(
            MovieRepository movieRepository,
            MovieMapper movieMapper,
            ReactiveRedisTemplate<String, Movie> reactiveRedisTemplate) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.stringMovieReactiveValueOperations = reactiveRedisTemplate.opsForValue();
    }

    public Flux<MovieResponse> findAll() {
        return reactiveRedisTemplate
                .keys(AppConstants.MOVIE_KEY + "*")
                // Fetching cached movies.
                .flatMap(stringMovieReactiveValueOperations::get)
                // If cache is empty, fetch the database for movies
                .switchIfEmpty(movieRepository
                        .findAll()
                        // Persisting the fetched movies in the cache.
                        .doOnNext(
                                movie -> Mono.defer(() -> stringMovieReactiveValueOperations.set(
                                                AppConstants.MOVIE_KEY + movie.id(), movie))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .subscribe() // Asynchronously update the cache
                                ))
                .map(movieMapper::toResponse);
    }

    public Mono<MovieResponse> findMovieById(Long id) {
        return stringMovieReactiveValueOperations
                .get(AppConstants.MOVIE_KEY + id)
                .switchIfEmpty(movieRepository
                        .findById(id)
                        .doOnNext(
                                movie -> Mono.defer(() -> stringMovieReactiveValueOperations.set(
                                                AppConstants.MOVIE_KEY + movie.id(), movie))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .subscribe() // Asynchronously update the cache
                                ))
                .map(movieMapper::toResponse);
    }

    @Transactional
    public Flux<Movie> saveAllMovies(Flux<Movie> movieFlux) {
        return movieRepository
                .saveAll(movieFlux)
                .doOnNext(
                        movie -> Mono.defer(() -> stringMovieReactiveValueOperations.set(
                                        AppConstants.MOVIE_KEY + movie.id(), movie))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe() // Asynchronously update the cache
                        );
    }

    @Transactional
    public Mono<MovieResponse> saveMovie(MovieRequest movieRequest) {
        return Mono.just(movieMapper.toEntity(movieRequest))
                .flatMap(movieRepository::save)
                .doOnNext(
                        movie -> Mono.defer(() -> stringMovieReactiveValueOperations.set(
                                        AppConstants.MOVIE_KEY + movie.id(), movie))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe() // Asynchronously update the cache
                        )
                .map(movieMapper::toResponse);
    }

    @Transactional
    public Mono<MovieResponse> updateMovie(Long id, MovieRequest movieRequest) {
        return movieRepository
                .findById(id)
                .map(movie -> movieMapper.mapMovieWithRequest(movie, movieRequest))
                .flatMap(movieRepository::save)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(
                        movie -> Mono.defer(() -> stringMovieReactiveValueOperations.set(
                                        AppConstants.MOVIE_KEY + movie.id(), movie))
                                .subscribe() // Asynchronously update the cache
                        )
                .map(movieMapper::toResponse);
    }

    @Transactional
    public Mono<Long> deleteMovieById(Long id) {
        return movieRepository.deleteById(id).then(reactiveRedisTemplate.delete(AppConstants.MOVIE_KEY + id));
    }

    @Transactional
    public Mono<String> deleteAll() {
        return movieRepository
                .deleteAll()
                .then(reactiveRedisTemplate
                        .getConnectionFactory()
                        .getReactiveConnection()
                        .serverCommands()
                        .flushDb()
                        .single());
    }
}
