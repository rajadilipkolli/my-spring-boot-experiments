package com.example.cache.web.controllers;

import com.example.cache.exception.MovieNotFoundException;
import com.example.cache.model.query.FindMoviesQuery;
import com.example.cache.model.request.MovieRequest;
import com.example.cache.model.response.MovieResponse;
import com.example.cache.model.response.PagedResult;
import com.example.cache.services.MovieService;
import com.example.cache.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/movies")
@Slf4j
@RequiredArgsConstructor
class MovieController {

    private final MovieService movieService;

    @GetMapping
    PagedResult<MovieResponse> getAllMovies(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        FindMoviesQuery findMoviesQuery = new FindMoviesQuery(pageNo, pageSize, sortBy, sortDir);
        return movieService.findAllMovies(findMoviesQuery);
    }

    @GetMapping("/{id}")
    ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return movieService.findMovieById(id).map(ResponseEntity::ok).orElseThrow(() -> new MovieNotFoundException(id));
    }

    @PostMapping
    ResponseEntity<MovieResponse> createMovie(@RequestBody @Validated MovieRequest movieRequest) {
        MovieResponse response = movieService.saveMovie(movieRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/movies/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @RequestBody @Valid MovieRequest movieRequest) {
        return ResponseEntity.ok(movieService.updateMovie(id, movieRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<MovieResponse> deleteMovie(@PathVariable Long id) {
        return movieService
                .findMovieById(id)
                .map(movie -> {
                    movieService.deleteMovieById(id);
                    return ResponseEntity.ok(movie);
                })
                .orElseThrow(() -> new MovieNotFoundException(id));
    }
}
