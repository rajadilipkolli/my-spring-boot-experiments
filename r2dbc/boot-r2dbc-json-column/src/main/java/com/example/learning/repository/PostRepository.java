package com.example.learning.repository;

import com.example.learning.entity.Post;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository extends R2dbcRepository<Post, UUID>, ReactiveQueryByExampleExecutor<Post> {

    @Query("SELECT * FROM posts where title like :title")
    Flux<Post> findByTitleContains(String title);

    Mono<Long> countByTitleContaining(String title);

    Flux<PostSummary> findByTitleLike(String title, Pageable pageable);
}
