package com.example.graphql.web.controllers;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.services.AuthorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public List<AuthorEntity> getAllAuthors() {
        return authorService.findAllAuthors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorEntity> getAuthorById(@PathVariable Long id) {
        return authorService
                .findAuthorById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorEntity createAuthor(@RequestBody @Validated AuthorEntity author) {
        return authorService.saveAuthor(author);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorEntity> updateAuthor(
            @PathVariable Long id, @RequestBody AuthorEntity author) {
        return authorService
                .findAuthorById(id)
                .map(
                        authorObj -> {
                            author.setId(id);
                            return ResponseEntity.ok(authorService.saveAuthor(author));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AuthorEntity> deleteAuthor(@PathVariable Long id) {
        return authorService
                .findAuthorById(id)
                .map(
                        author -> {
                            authorService.deleteAuthorById(id);
                            return ResponseEntity.ok(author);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
