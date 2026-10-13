package com.example.jooq.spring.web.controller;

import com.example.jooq.spring.model.request.CreateAuthorRequest;
import com.example.jooq.spring.model.response.AuthorResponse;
import com.example.jooq.spring.model.response.AuthorWithBooksResponse;
import com.example.jooq.spring.service.AuthorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    /** Creates the author endpoints using the supplied service. */
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Lists a page of authors in ascending ID order.
     *
     * @param offset number of rows to skip; defaults to zero for HTTP requests
     * @param limit maximum number of rows to return; defaults to ten for HTTP requests
     * @return the selected authors, or an empty list if the page has no rows
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @GetMapping
    public List<AuthorResponse> getAllAuthors(
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        return authorService.findAll(offset, limit);
    }

    /**
     * Returns HTTP 200 with the requested author.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the author does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.findById(id));
    }

    /**
     * Returns HTTP 200 with the author and its books, which may be empty and have unspecified order.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the author does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataAccessException if a database query fails
     */
    @GetMapping("/{id}/books")
    public ResponseEntity<AuthorWithBooksResponse> getAuthorWithBooks(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.findAuthorWithBooks(id));
    }

    /**
     * Creates an author and returns HTTP 201 with its values and a Location header for its URL.
     *
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are
     *     violated; the exception handler converts this to HTTP 409
     * @throws org.springframework.dao.DataAccessException if another database failure occurs
     */
    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        AuthorResponse created = authorService.createAuthor(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Replaces the author's name and email and returns HTTP 200 with its saved values.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the author does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are
     *     violated; the exception handler converts this to HTTP 409
     * @throws org.springframework.dao.DataAccessException if another database failure occurs
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long id, @Valid @RequestBody CreateAuthorRequest request) {
        return ResponseEntity.ok(authorService.updateAuthor(id, request));
    }

    /**
     * Deletes the author and its books and returns HTTP 204 with no body.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the author does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataAccessException if a database operation fails
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
