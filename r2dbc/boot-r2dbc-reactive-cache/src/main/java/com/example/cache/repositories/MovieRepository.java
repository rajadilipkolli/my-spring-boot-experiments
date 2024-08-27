package com.example.cache.repositories;

import com.example.cache.entities.Movie;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MovieRepository extends ReactiveCrudRepository<Movie, Long> {}
