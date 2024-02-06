/*
package com.example.locks.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.entities.Movie;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.repositories.MovieRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class MovieControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    private List<Movie> movieList = null;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAllInBatch();

        movieList = new ArrayList<>();
        movieList.add(new Movie(null, "First Movie"));
        movieList.add(new Movie(null, "Second Movie"));
        movieList.add(new Movie(null, "Third Movie"));
        movieList = movieRepository.saveAll(movieList);
    }

    @Test
    void shouldFetchAllMovies() throws Exception {
        this.mockMvc
                .perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(movieList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindMovieById() throws Exception {
        Movie movie = movieList.getFirst();
        Long movieId = movie.getId();

        this.mockMvc
                .perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(movie.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(movie.getText())));
    }

    @Test
    void shouldCreateNewMovie() throws Exception {
        MovieRequest movieRequest = new MovieRequest("New Movie");
        this.mockMvc
                .perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(movieRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewMovieWithoutText() throws Exception {
        MovieRequest movieRequest = new MovieRequest(null);

        this.mockMvc
                .perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/movies")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateMovie() throws Exception {
        Long movieId = movieList.getFirst().getId();
        MovieRequest movieRequest = new MovieRequest("Updated Movie");

        this.mockMvc
                .perform(put("/api/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(movieId), Long.class))
                .andExpect(jsonPath("$.text", is(movieRequest.text())));
    }

    @Test
    void shouldDeleteMovie() throws Exception {
        Movie movie = movieList.getFirst();

        this.mockMvc
                .perform(delete("/api/movies/{id}", movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(movie.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(movie.getText())));
    }
}
*/
