package com.example.cache.config;

import com.example.cache.entities.Movie;
import com.example.cache.services.MovieService;
import com.example.cache.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

@Configuration(proxyBeanMethods = false)
@Profile(AppConstants.PROFILE_NOT_TEST)
class Initializer {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    @Bean
    ApplicationRunner saveMovies(MovieService movieService) {
        Flux<Movie> movies = Flux.just(
                new Movie(null, "DJ Tillu"),
                new Movie(null, "Tillu Square"),
                new Movie(null, " Om Bheem Bush"),
                new Movie(null, "Aa Okkati Adakku"),
                new Movie(null, " Bhimaa"));
        return args -> movieService
                .deleteAll()
                .thenMany(movieService.saveAllMovies(movies))
                .subscribe(movie -> log.info(movie.toString()));
    }
}
