package com.example.cache.config;

import com.example.cache.entities.Movie;
import com.example.cache.repositories.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration(proxyBeanMethods = false)
class Initializer {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    @Bean
    public ApplicationRunner saveMovies(MovieRepository repository) {
        Flux<Movie> movies = Flux.just(
                new Movie(null, "DJ Tillu"),
                new Movie(null, "Tillu Square"),
                new Movie(null, " Om Bheem Bush"),
                new Movie(null, "Aa Okkati Adakku"),
                new Movie(null, " Bhimaa"));
        return args -> repository
                .deleteAll()
                .thenMany(repository.saveAll(movies))
                .subscribe(movie -> log.info(movie.toString()));
    }
}
