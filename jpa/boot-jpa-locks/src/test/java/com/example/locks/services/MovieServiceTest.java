/*
package com.example.locks.services;


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
        given(movieMapper.toResponse(any(Movie.class))).willReturn(getMovieResponse());
        // when
        Optional<MovieResponse> optionalMovie = movieService.findMovieById(1L);
        // then
        assertThat(optionalMovie).isPresent();
        MovieResponse movie = optionalMovie.get();
        assertThat(movie.id()).isEqualTo(1L);
        assertThat(movie.text()).isEqualTo("junitTest");
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
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setText("junitTest");
        return movie;
    }

    private MovieResponse getMovieResponse() {
        return new MovieResponse(1L, "junitTest");
    }
}
*/
