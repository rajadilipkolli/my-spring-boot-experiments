package com.example.jooq.r2dbc.repository.custom;

import com.example.jooq.r2dbc.model.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CustomPostRepository {
    Mono<Page<PostResponse>> findByKeyword(String keyword, Pageable pageable);
}
