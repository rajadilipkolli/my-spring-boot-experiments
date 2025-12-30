package com.example.learning.repository;

import com.example.learning.entity.Post;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository extends R2dbcRepository<Post, UUID> {

    @Query("SELECT * FROM posts where title like :title")
    Flux<Post> findByTitleContains(String title);

    Mono<Long> countByTitleContaining(String title);

    Flux<PostSummary> findByTitleLike(String title, Pageable pageable);

    @Query("""
            SELECT * FROM posts ORDER BY CASE WHEN :direction = 'ASC' THEN :sortBy END ASC,
            CASE WHEN :direction = 'DESC' THEN :sortBy END DESC
            LIMIT :size OFFSET :offset
            """)
    Flux<Post> findAllWithPagination(
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("sortBy") String sortBy,
            @Param("direction") String direction);
}
