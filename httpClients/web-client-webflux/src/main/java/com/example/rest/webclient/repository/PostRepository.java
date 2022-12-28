package com.example.rest.webclient.repository;

import com.example.rest.webclient.model.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PostRepository extends ReactiveCrudRepository<Post, Long> {}
