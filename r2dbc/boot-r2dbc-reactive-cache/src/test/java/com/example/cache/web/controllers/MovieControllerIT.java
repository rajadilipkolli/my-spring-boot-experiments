package com.example.cache.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cache.common.AbstractIntegrationTest;
import com.example.cache.entities.Movie;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import com.example.cache.repositories.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class MovieControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    private Flux<Movie> movieFlux = null;

    @BeforeEach
    void setUp() {
        movieFlux = movieRepository
                .deleteAll()
                .thenMany(Flux.just(
                        new Movie(null, "First Movie"),
                        new Movie(null, "Second Movie"),
                        new Movie(null, "Third Movie")))
                .flatMap(movieRepository::save);
    }

    @Test
    void shouldFetchAllMovies() {
        this.webTestClient
                .get()
                .uri("/api/movies")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieResponse.class)
                .hasSize(movieFlux.collectList().block().size());
    }

    @Test
    void shouldFindMovieById() {
        Movie movie = movieFlux.blockLast();
        Long movieId = movie.id();

        this.webTestClient
                .get()
                .uri("/api/movies/{id}", movieId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(movieId)
                .jsonPath("$.title")
                .isEqualTo(movie.title());
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingMovie() {
        this.webTestClient
                .get()
                .uri("/api/movies/{id}", 10000)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.boot-reactive-cache.com/errors/not-found","title":"Not Found",
                        "status":404,"detail":"Movie with Id '10000' not found","instance":"/api/movies/10000",
                        "properties":{"errorCategory":"Generic"}}
                        """);
    }

    @Test
    void shouldCreateNewMovie() {
        MovieRequest movieRequest = new MovieRequest("New Movie");
        this.webTestClient
                .post()
                .uri("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(movieRequest), MovieRequest.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectHeader()
                .contentLength(29)
                .expectHeader()
                .exists("Location")
                .expectBody(MovieResponse.class)
                .value(movieResponse -> {
                    assertThat(movieResponse.id()).isNotNull();
                    assertThat(movieResponse.title()).isNotNull().isEqualTo("New Movie");
                });
    }

    @Test
    void shouldReturn400WhenCreateNewMovieWithoutTitle() {
        MovieRequest movieRequest = new MovieRequest(null);

        this.webTestClient
                .post()
                .uri("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(movieRequest), MovieRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.boot-reactive-cache.com/errors/constraint-violation",
                        "title":"Constraint Violation","status":400,"detail":"Invalid request content.",
                        "instance":"/api/movies",
                        "properties":{"violations":[{"object":"movieRequest","field":"title","rejectedValue":null,
                        "message":"Title cannot be blank"}]}}
                        """,
                        true);
    }

    @Test
    void shouldUpdateMovie() {
        Long movieId = movieFlux.blockLast().id();
        MovieRequest movieRequest = new MovieRequest("Updated Movie");

        this.webTestClient
                .put()
                .uri("/api/movies/{id}", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(movieRequest))
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(movieId)
                .jsonPath("$.title")
                .isEqualTo("Updated Movie");
    }

    @Test
    void shouldDeleteMovie() {
        Movie movie = movieFlux.blockLast();

        this.webTestClient
                .delete()
                .uri("/api/movies/{id}", movie.id())
                .exchange()
                .expectStatus()
                .isAccepted()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(movie.id())
                .jsonPath("$.title")
                .isEqualTo(movie.title());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingMovie() {
        this.webTestClient
                .delete()
                .uri("/api/movies/{id}", 10000)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                        {"type":"https://api.boot-reactive-cache.com/errors/not-found","title":"Not Found","status":404,
                        "detail":"Movie with Id '10000' not found","instance":"/api/movies/10000",
                        "properties":{"errorCategory":"Generic"}}
                        """);
    }
}
