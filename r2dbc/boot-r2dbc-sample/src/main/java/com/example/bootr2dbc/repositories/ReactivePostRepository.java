package com.example.bootr2dbc.repositories;

import com.example.bootr2dbc.entities.ReactivePost;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactivePostRepository extends ReactiveCrudRepository<ReactivePost, Long> {}
