package com.example.highrps.web.controller;

import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.response.AuthorResponse;
import com.example.highrps.service.AuthorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/author/{email}")
    public ResponseEntity<AuthorResponse> getAuthorByEmail(@PathVariable String email) {
        AuthorResponse resp = authorService.findAuthorByEmail(email);
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/author")
    public ResponseEntity<AuthorResponse> createAuthor(@RequestBody @Valid AuthorRequest newAuthorRequest) {
        AuthorRequest withCreatedAt = newAuthorRequest.withTimestamps(LocalDateTime.now(), null);
        AuthorResponse resp = authorService.saveOrUpdateAuthor(withCreatedAt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(resp.email())
                .toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @PutMapping(value = "/author/{email}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable String email, @RequestBody @Valid AuthorRequest newAuthorRequest) {
        AuthorResponse resp = authorService.updateAuthor(email, newAuthorRequest);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/author/{email}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable @NotBlank String email) {
        authorService.deleteAuthor(email);
        return ResponseEntity.noContent().build();
    }
}
