package com.example.jooq.r2dbc.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.example.jooq.r2dbc.config.logging.Loggable;
import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.request.TagDto;
import com.example.jooq.r2dbc.service.TagService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class TagHandler {

    private final TagService tagService;

    @Loggable
    public Mono<ServerResponse> getAll(ServerRequest req) {
        return ok().body(this.tagService.findAll(), Tags.class);
    }

    @Loggable
    public Mono<ServerResponse> get(ServerRequest req) {
        return this.tagService
                .findById(req.pathVariable("id"))
                .flatMap(tags -> ServerResponse.ok().body(Mono.just(tags), Tags.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Loggable
    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(TagDto.class)
                .flatMap(this.tagService::create)
                .flatMap(tag -> created(URI.create("/tags/" + tag.getId())).build());
    }
}
