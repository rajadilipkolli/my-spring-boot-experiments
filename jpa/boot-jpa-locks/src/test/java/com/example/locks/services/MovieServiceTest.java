package com.example.locks.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.locks.entities.Movie;
import com.example.locks.mapper.JpaLocksMapper;
import com.example.locks.model.response.MovieResponse;
import com.example.locks.repositories.MovieRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private JpaLocksMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    @Test
    void findMovieById() {
        // given
        given(movieRepository.findById(1L)).willReturn(Optional.of(getMovie()));
        given(movieMapper.movieToMovieResponse(any(Movie.class))).willReturn(getMovieResponse());
        // when
        Optional<MovieResponse> optionalMovie = movieService.findMovieById(1L);
        // then
        assertThat(optionalMovie).isPresent();
        MovieResponse movie = optionalMovie.get();
        assertThat(movie.movieId()).isOne();
        assertThat(movie.movieTitle()).isEqualTo("junitTest");
    }

    @Test
    void deleteMovieById() {
        // given
        willDoNothing().given(movieRepository).deleteById(1L);
        // when
        movieService.deleteMovieById(1L);
        // then
        verify(movieRepository, times(1)).deleteById(1L);
    }

    private Movie getMovie() {
        return new Movie().setMovieId(1L).setMovieTitle("junitTest").setReleaseDate(LocalDate.of(2024, 12, 24));
    }

    private MovieResponse getMovieResponse() {
        return new MovieResponse(
                1L,
                "junitTest",
                LocalDate.of(2024, 12, 24),
                BigDecimal.TEN,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }
}
