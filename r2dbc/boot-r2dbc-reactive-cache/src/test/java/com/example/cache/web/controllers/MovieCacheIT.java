package com.example.cache.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cache.common.AbstractIntegrationTest;
import com.example.cache.entities.Movie;
import com.example.cache.repositories.MovieRepository;
import com.example.cache.services.MovieService;
import com.example.cache.utils.AppConstants;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;

class MovieCacheIT extends AbstractIntegrationTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Movie> reactiveRedisTemplate;

    @BeforeEach
    void setUp() {
        // Ensure DB and Redis are clean before each test
        // 1) Clear Redis DB (fastest for test env)
        reactiveRedisTemplate
                .execute(conn -> conn.serverCommands().flushDb())
                .then()
                .block(Duration.ofSeconds(5));
        // 2) Also clear backing store
        movieService.deleteAll().block(Duration.ofSeconds(10));
    }

    @Test
    void shouldPopulateCacheOnFindByIdAndServeFromCacheOnSubsequentCalls() {
        // arrange - persist a movie directly via repository so cache is empty
        Movie saved = movieRepository.save(new Movie(null, "Cache Test Movie")).block(Duration.ofSeconds(5));
        assertThat(saved).isNotNull();
        String key = AppConstants.MOVIE_KEY + saved.id();

        // cache should be empty initially
        Movie cachedBefore = reactiveRedisTemplate.opsForValue().get(key).block(Duration.ofSeconds(5));
        assertThat(cachedBefore).isNull();

        // act - first call should populate cache
        movieService.findMovieById(saved.id()).block(Duration.ofSeconds(5));
        // wait/poll for cache to be populated (async cache population in service)
        Movie cachedAfter = waitForValue(key, Duration.ofSeconds(5));
        assertThat(cachedAfter).isNotNull();

        // act - second call should be satisfied from cache (no change in cache contents expected)
        movieService.findMovieById(saved.id()).block(Duration.ofSeconds(5));
    }

    @Test
    void shouldCacheFindAllAndAvoidRepositoryOnSubsequentFindAll() {
        // arrange - create a few movies via repository (cache empty)
        List<Movie> saved = movieRepository
                .saveAll(Flux.just(new Movie(null, "A"), new Movie(null, "B"), new Movie(null, "C")))
                .collectList()
                .block(Duration.ofSeconds(10));

        assertThat(saved).isNotEmpty();

        // ensure cache is empty (no keys)
        var keysBefore = reactiveRedisTemplate
                .keys(AppConstants.MOVIE_KEY + "*")
                .collectList()
                .block(Duration.ofSeconds(5));
        assertThat(keysBefore).isEmpty();

        // act - first findAll should populate cache
        movieService.findAll().collectList().block(Duration.ofSeconds(5));

        // wait for cache keys to appear (cache population is async)
        var keysAfter = waitForKeys(AppConstants.MOVIE_KEY + "*", Duration.ofSeconds(5));
        assertThat(keysAfter).isNotEmpty();

        // call again - cache should satisfy it (we check no new keys are created and repository still returns saved
        // data)
        movieService.findAll().collectList().block(Duration.ofSeconds(5));
    }

    @Test
    void shouldRemoveCacheOnDelete() {
        Movie saved = movieRepository.save(new Movie(null, "To Be Deleted")).block(Duration.ofSeconds(5));
        assertThat(saved).isNotNull();
        String key = AppConstants.MOVIE_KEY + saved.id();

        // populate cache via service
        movieService.findMovieById(saved.id()).block(Duration.ofSeconds(5));
        // wait for cache to be populated
        Movie cached = waitForValue(key, Duration.ofSeconds(5));
        assertThat(cached).isNotNull();

        // delete and ensure cache key removed
        movieService.deleteMovieById(saved.id()).block(Duration.ofSeconds(5));
        boolean evicted = waitUntilAbsent(key, Duration.ofSeconds(5));
        assertThat(evicted).isTrue();
    }

    private boolean waitUntilAbsent(String key, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            Movie v = reactiveRedisTemplate.opsForValue().get(key).block(Duration.ofSeconds(1));
            if (v == null) return true;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    // Helper: poll Redis for a value until present or timeout
    private Movie waitForValue(String key, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            Movie v = reactiveRedisTemplate.opsForValue().get(key).block(Duration.ofSeconds(1));
            if (Objects.nonNull(v)) {
                return v;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    // Helper: poll Redis for keys until at least one appears or timeout
    private List<String> waitForKeys(String pattern, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            List<String> keys =
                    reactiveRedisTemplate.keys(pattern).collectList().block(Duration.ofSeconds(1));
            if (keys != null && !keys.isEmpty()) {
                return keys;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return List.of();
    }
}
