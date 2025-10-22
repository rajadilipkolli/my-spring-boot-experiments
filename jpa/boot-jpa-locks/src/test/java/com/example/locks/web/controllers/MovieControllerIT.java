package com.example.locks.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.locks.common.AbstractIntegrationTest;
import com.example.locks.entities.Director;
import com.example.locks.entities.Genre;
import com.example.locks.entities.Movie;
import com.example.locks.model.request.DirectorRequest;
import com.example.locks.model.request.GenreRequest;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.model.request.ReviewRequest;
import com.example.locks.repositories.MovieRepository;
import com.example.locks.repositories.ReviewsRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Autowired
    private ReviewsRepository reviewRepository;

    private List<Movie> movieList = null;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();

        movieList = new ArrayList<>();
        movieList.add(new Movie()
                .setMovieTitle("First Movie")
                .setDirector(new Director(null, "First Director", LocalDate.now(), "indian", new ArrayList<>()))
                .setGenres(List.of(new Genre().setGenreName("Comedy"))));
        movieList.add(new Movie().setMovieTitle("Second Movie"));
        movieList.add(new Movie().setMovieTitle("Third Movie"));
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
        Long movieId = movie.getMovieId();

        this.mockMvc
                .perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is(movie.getMovieId()), Long.class))
                .andExpect(jsonPath("$.movieTitle", is(movie.getMovieTitle())));
    }

    @Test
    void shouldCreateNewMovie() throws Exception {
        MovieRequest movieRequest = new MovieRequest(
                "New Movie",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                new DirectorRequest("New Director", LocalDate.now().minusYears(50), "Indian"),
                new ArrayList<>(),
                List.of(new ReviewRequest(5.0D, "Excellent"), new ReviewRequest(4.5D, "Super")),
                List.of(new GenreRequest("Comedy")));
        this.mockMvc
                .perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.movieId", notNullValue()))
                .andExpect(jsonPath("$.movieTitle", is(movieRequest.movieTitle())))
                .andExpect(
                        jsonPath("$.releaseDate", is(LocalDate.of(2024, 12, 24).toString())))
                .andExpect(jsonPath("$.director.directorName")
                        .value(movieRequest.director().directorName()))
                .andExpect(jsonPath("$.director.nationality")
                        .value(movieRequest.director().nationality()))
                .andExpect(jsonPath("$.actors", is(empty())))
                .andExpect(jsonPath("$.reviews.size()", is(2)))
                .andExpect(jsonPath("$.reviews[0].rating", is(in(new Double[] {5.0D, 4.5D}))))
                .andExpect(jsonPath("$.reviews[1].rating", is(in(new Double[] {5.0D, 4.5D}))))
                .andExpect(jsonPath("$.genres[0].genreName").value("Comedy"));
    }

    @Test
    void shouldReturn400WhenCreateNewMovieWithoutTitle() throws Exception {
        MovieRequest movieRequest = new MovieRequest(null, null, null, null, null, null, null);

        this.mockMvc
                .perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jpa-locks.com/errors/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/movies")))
                .andExpect(jsonPath("$.properties.violations", hasSize(1)))
                .andExpect(jsonPath("$.properties.violations[0].field", is("movieTitle")))
                .andExpect(jsonPath("$.properties.violations[0].message", is("MovieTitle cant be Blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateMovie() throws Exception {
        Movie movie = movieList.getFirst();
        MovieRequest movieRequest = new MovieRequest(
                "Updated Movie",
                movie.getReleaseDate(),
                movie.getBudget(),
                new DirectorRequest(
                        movie.getDirector().getDirectorName(),
                        movie.getDirector().getDob(),
                        movie.getDirector().getNationality()),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        this.mockMvc
                .perform(put("/api/movies/{id}", movie.getMovieId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is(movie.getMovieId()), Long.class))
                .andExpect(jsonPath("$.movieTitle", is(movieRequest.movieTitle())))
                .andExpect(jsonPath("$.director.directorName", is("First Director")))
                .andExpect(jsonPath("$.actors", is(empty())))
                .andExpect(jsonPath("$.reviews", is(empty())))
                .andExpect(jsonPath("$.genres", is(empty())));
    }

    @Test
    void shouldDeleteMovie() throws Exception {
        Movie movie = movieList.getFirst();

        this.mockMvc
                .perform(delete("/api/movies/{id}", movie.getMovieId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is(movie.getMovieId()), Long.class))
                .andExpect(jsonPath("$.movieTitle", is(movie.getMovieTitle())));
    }
}
