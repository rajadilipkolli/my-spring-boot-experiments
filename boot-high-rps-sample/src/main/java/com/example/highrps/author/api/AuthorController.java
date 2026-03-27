package com.example.highrps.author.api;

import com.example.highrps.author.AuthorRequest;
import com.example.highrps.author.command.AuthorCommandResult;
import com.example.highrps.author.command.AuthorCommandService;
import com.example.highrps.author.command.CreateAuthorCommand;
import com.example.highrps.author.command.UpdateAuthorCommand;
import com.example.highrps.author.query.AuthorProjection;
import com.example.highrps.author.query.AuthorQuery;
import com.example.highrps.author.query.AuthorQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
public class AuthorController {

    private final AuthorCommandService authorCommandService;
    private final AuthorQueryService authorQueryService;

    public AuthorController(AuthorCommandService authorCommandService, AuthorQueryService authorQueryService) {
        this.authorCommandService = authorCommandService;
        this.authorQueryService = authorQueryService;
    }

    @GetMapping("/author/{email}")
    public ResponseEntity<AuthorProjection> getAuthorByEmail(@PathVariable String email) {
        AuthorProjection resp = authorQueryService.getAuthor(new AuthorQuery(email.toLowerCase(Locale.ROOT)));
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/author")
    public ResponseEntity<AuthorCommandResult> createAuthor(@RequestBody @Valid AuthorRequest newAuthorRequest) {
        CreateAuthorCommand cmd = new CreateAuthorCommand(
                newAuthorRequest.email().toLowerCase(Locale.ROOT),
                newAuthorRequest.firstName(),
                newAuthorRequest.middleName(),
                newAuthorRequest.lastName(),
                newAuthorRequest.mobile(),
                LocalDateTime.now());
        AuthorCommandResult resp = authorCommandService.createAuthor(cmd);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(newAuthorRequest.email())
                .toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @PutMapping(value = "/author/{email}")
    public ResponseEntity<AuthorCommandResult> updateAuthor(
            @PathVariable String email, @RequestBody @Valid AuthorRequest newAuthorRequest) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        UpdateAuthorCommand cmd = new UpdateAuthorCommand(
                normalizedEmail,
                newAuthorRequest.firstName(),
                newAuthorRequest.middleName(),
                newAuthorRequest.lastName(),
                newAuthorRequest.mobile(),
                LocalDateTime.now());
        AuthorCommandResult resp = authorCommandService.updateAuthor(cmd);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/author/{email}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable @NotBlank String email) {
        authorCommandService.deleteAuthor(email);
        return ResponseEntity.noContent().build();
    }
}
