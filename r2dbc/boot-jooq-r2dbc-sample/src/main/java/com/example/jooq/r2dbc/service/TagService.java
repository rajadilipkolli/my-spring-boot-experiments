package com.example.jooq.r2dbc.service;

import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.request.TagDto;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.repository.TagRepository;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Mono<PaginatedResult<Tags>> findAll(Pageable pageable) {
        return this.tagRepository.findAll(pageable).map(PaginatedResult::new);
    }

    public Mono<Tags> findById(String id) {
        return this.tagRepository.findById(UUID.fromString(id));
    }

    public Mono<Tags> create(TagDto tagDto) {
        return this.tagRepository.save(new Tags().setName(tagDto.name()));
    }
}
