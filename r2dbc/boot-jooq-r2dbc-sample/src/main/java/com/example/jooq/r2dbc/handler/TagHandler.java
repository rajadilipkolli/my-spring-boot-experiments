package com.example.jooq.r2dbc.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.example.jooq.r2dbc.config.logging.Loggable;
import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.model.request.TagDto;
import com.example.jooq.r2dbc.model.response.PaginatedResult;
import com.example.jooq.r2dbc.service.TagService;
import com.example.jooq.r2dbc.utils.AppConstants;
import java.net.URI;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Loggable
public class TagHandler {

    // Service responsible for handling Tag-related business logic
    private final TagService tagService;

    public TagHandler(TagService tagService) {
        this.tagService = tagService;
    }

    // Retrieve all tags based on query parameters for sorting and pagination
    public Mono<ServerResponse> getAll(ServerRequest req) {
        // Extracting and setting sort direction and field from query parameters
        String sortBy = req.queryParam("sortBy").orElse(AppConstants.DEFAULT_SORT_BY);
        String sortDir = req.queryParam("sortDir").orElse(AppConstants.DEFAULT_SORT_DIRECTION);
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Creating Pageable instance for pagination
        int pageSize =
                req.queryParam("pageSize")
                        .map(Integer::parseInt)
                        .orElse(AppConstants.DEFAULT_PAGE_SIZE);
        int pageNo =
                req.queryParam("pageNo")
                        .map(Integer::parseInt)
                        .orElse(AppConstants.DEFAULT_PAGE_NUMBER);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Returning paginated result of tags
        return ok().body(this.tagService.findAll(pageable), PaginatedResult.class);
    }

    // Retrieve a specific tag by its ID
    public Mono<ServerResponse> get(ServerRequest req) {
        return this.tagService
                .findById(req.pathVariable("id"))
                .flatMap(tags -> ServerResponse.ok().body(Mono.just(tags), Tags.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // Create a new tag based on the request body
    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(TagDto.class)
                .flatMap(this.tagService::create)
                .flatMap(tag -> created(URI.create("/tags/" + tag.getId())).build());
    }
}
