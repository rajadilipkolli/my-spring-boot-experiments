package com.example.jooq.spring.model.response;

import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import java.time.LocalDateTime;
import java.util.List;

public record AuthorWithBooksResponse(
        Long id, String name, String email, LocalDateTime createdAt, List<BookResponse> books) {

    public static AuthorWithBooksResponse from(AuthorsRecord author, List<BookResponse> books) {
        return new AuthorWithBooksResponse(
                author.getId(), author.getName(), author.getEmail(), author.getCreatedAt(), books);
    }
}
