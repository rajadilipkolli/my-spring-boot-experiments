package com.example.graphql.web.controllers;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.exception.AuthorNotFoundException;
import com.example.graphql.model.query.FindQuery;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.model.response.PagedResult;
import com.example.graphql.services.AuthorService;
import com.example.graphql.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/authors")
@Loggable
@Validated
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    ResponseEntity<PagedResult<AuthorResponse>> getAllAuthors(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        FindQuery findQuery = new FindQuery(pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(authorService.findAllAuthors(findQuery));
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull AuthorResponse> getAuthorById(@PathVariable Long id) {
        return authorService
                .findAuthorById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull AuthorResponse> createAuthor(@RequestBody @Valid AuthorRequest authorRequest) {
        AuthorResponse authorResponse = authorService.saveAuthor(authorRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(authorResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(authorResponse);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull AuthorResponse> updateAuthor(
            @PathVariable Long id, @RequestBody @Valid AuthorRequest authorRequest) {
        return authorService
                .updateAuthor(authorRequest, id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (authorService.existsAuthorById(id)) {
            authorService.deleteAuthorById(id);
            return ResponseEntity.accepted().build();
        } else {
            throw new AuthorNotFoundException(id);
        }
    }
}
