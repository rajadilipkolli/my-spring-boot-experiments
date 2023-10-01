package com.example.jooq.r2dbc.repository.custom;

import com.example.jooq.r2dbc.entities.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CustomTagRepository {

    Mono<Page<Tags>> findAll(Pageable pageable);
}
