package com.example.jooq.r2dbc.service;

import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.request.TagDto;
import com.example.jooq.r2dbc.repository.TagRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Flux<Tags> findAll() {
        return this.tagRepository.findAll();
    }

    public Mono<Tags> findById(String id) {
        return this.tagRepository.findById(UUID.fromString(id));
    }

    public Mono<Tags> create(TagDto tagDto) {
        return this.tagRepository.save(new Tags(tagDto.name()));
    }
}
