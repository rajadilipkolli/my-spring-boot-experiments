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

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorResponse> getAllAuthors(
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        return authorService.findAll(offset, limit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.findById(id));
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<AuthorWithBooksResponse> getAuthorWithBooks(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.findAuthorWithBooks(id));
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        AuthorResponse created = authorService.createAuthor(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long id, @Valid @RequestBody CreateAuthorRequest request) {
        return ResponseEntity.ok(authorService.updateAuthor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
