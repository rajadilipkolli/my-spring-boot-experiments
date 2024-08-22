package com.example.cache.web.controllers;

import com.example.cache.exception.MovieNotFoundException;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import com.example.cache.services.MovieService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/movies")
class MovieController {

    private final MovieService movieService;

    MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public Flux<MovieResponse> findAll() {
        return movieService.findAll();
    }

    @GetMapping("/{id}")
    Mono<ResponseEntity<MovieResponse>> getMovieById(@PathVariable Long id) {
        return movieService
                .findMovieById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(id)));
    }

    @PostMapping
    Mono<ResponseEntity<MovieResponse>> createMovie(
            @RequestBody @Validated MovieRequest movieRequest, UriComponentsBuilder uriBuilder) {
        return movieService.saveMovie(movieRequest).map(movieResponse -> {
            // Build the location URI
            String location = uriBuilder
                    .path("/api/movies/{id}")
                    .buildAndExpand(movieResponse.id())
                    .toUriString();

            // Create a ResponseEntity with the Location header and the saved
            // ReactivePost
            return ResponseEntity.created(URI.create(location)).body(movieResponse);
        });
    }

    @PutMapping("/{id}")
    Mono<ResponseEntity<MovieResponse>> updateMovie(
            @PathVariable Long id, @RequestBody @Valid MovieRequest movieRequest) {
        return movieService
                .findMovieById(id)
                .flatMap(movieResponse -> movieService.updateMovie(movieResponse.id(), movieRequest))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(id)));
    }

    @DeleteMapping("/{id}")
    Mono<ResponseEntity<MovieResponse>> deleteMovie(@PathVariable Long id) {
        return movieService
                .findMovieById(id)
                .flatMap(movie -> movieService
                        .deleteMovieById(id)
                        .thenReturn(ResponseEntity.accepted().body(movie)))
                .switchIfEmpty(Mono.error(new MovieNotFoundException(id)));
    }
}
