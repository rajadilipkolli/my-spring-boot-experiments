package com.example.locks.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.locks.entities.Movie;
import com.example.locks.exception.MovieNotFoundException;
import com.example.locks.model.query.FindMoviesQuery;
import com.example.locks.model.request.MovieRequest;
import com.example.locks.model.response.MovieResponse;
import com.example.locks.model.response.PagedResult;
import com.example.locks.services.MovieService;
import com.example.locks.utils.AppConstants;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = MovieController.class)
@ActiveProfiles(AppConstants.PROFILE_TEST)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Movie> movieList;

    @BeforeEach
    void setUp() {
        this.movieList = new ArrayList<>();
        this.movieList.add(new Movie().setMovieId(1L).setMovieTitle("text 1"));
        this.movieList.add(new Movie().setMovieId(2L).setMovieTitle("text 2"));
        this.movieList.add(new Movie().setMovieId(3L).setMovieTitle("text 3"));
    }

    @Test
    void shouldFetchAllMovies() throws Exception {

        Page<Movie> page = new PageImpl<>(movieList);
        PagedResult<MovieResponse> moviePagedResult = new PagedResult<>(page, getMovieResponseList());
        FindMoviesQuery findMoviesQuery = new FindMoviesQuery(0, 10, "movieId", "asc");
        given(movieService.findAllMovies(findMoviesQuery)).willReturn(moviePagedResult);

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
        Long movieId = 1L;
        MovieResponse movie = new MovieResponse(
                movieId,
                "junitTest",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        given(movieService.findMovieById(movieId)).willReturn(Optional.of(movie));

        this.mockMvc
                .perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle", is(movie.movieTitle())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingMovie() throws Exception {
        Long movieId = 1L;
        given(movieService.findMovieById(movieId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jpa-locks.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Movie with Id '%d' not found".formatted(movieId)));
    }

    @Test
    void shouldCreateNewMovie() throws Exception {

        MovieResponse movie = new MovieResponse(
                1L,
                "junitTest",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        MovieRequest movieRequest = new MovieRequest(
                "junitTest",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        given(movieService.saveMovie(any(MovieRequest.class))).willReturn(movie);

        this.mockMvc
                .perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.movieId", notNullValue()))
                .andExpect(jsonPath("$.movieTitle", is(movie.movieTitle())));
    }

    @Test
    void shouldReturn400WhenCreateNewMovieWithoutText() throws Exception {
        MovieRequest movieRequest = new MovieRequest(null, null, null, null, null, null, null);

        this.mockMvc
                .perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
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
        Long movieId = 1L;
        MovieResponse movie = new MovieResponse(
                movieId,
                "updatedTitle",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        MovieRequest movieRequest = new MovieRequest(
                "UpdatedTitle",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        given(movieService.updateMovie(eq(movieId), any(MovieRequest.class))).willReturn(movie);

        this.mockMvc
                .perform(put("/api/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is(movieId), Long.class))
                .andExpect(jsonPath("$.movieTitle", is(movie.movieTitle())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingMovie() throws Exception {
        Long movieId = 1L;
        MovieRequest movieRequest = new MovieRequest(
                "junitTest",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        given(movieService.updateMovie(eq(movieId), any(MovieRequest.class)))
                .willThrow(new MovieNotFoundException(movieId));

        this.mockMvc
                .perform(put("/api/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jpa-locks.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Movie with Id '%d' not found".formatted(movieId)));
    }

    @Test
    void shouldDeleteMovie() throws Exception {
        Long movieId = 1L;
        MovieResponse movie = new MovieResponse(
                1L,
                "junitTest",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        given(movieService.findMovieById(movieId)).willReturn(Optional.of(movie));
        doNothing().when(movieService).deleteMovieById(movieId);

        this.mockMvc
                .perform(delete("/api/movies/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle", is(movie.movieTitle())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingMovie() throws Exception {
        Long movieId = 1L;
        given(movieService.findMovieById(movieId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/movies/{id}", movieId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-jpa-locks.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Movie with Id '%d' not found".formatted(movieId)));
    }

    List<MovieResponse> getMovieResponseList() {
        return movieList.stream()
                .map(movie -> new MovieResponse(
                        movie.getMovieId(),
                        movie.getMovieTitle(),
                        movie.getReleaseDate(),
                        movie.getBudget(),
                        null,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()))
                .toList();
    }
}
