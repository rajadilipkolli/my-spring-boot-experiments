package com.example.jooq.spring.model.response;

import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import java.time.OffsetDateTime;
import java.util.List;

public record AuthorWithBooksResponse(
        Long id, String name, String email, OffsetDateTime createdAt, List<BookResponse> books) {

    /**
     * Combines author record values with the supplied book responses.
     *
     * @param books associated book responses, retained without copying or querying the database
     */
    public static AuthorWithBooksResponse from(AuthorsRecord author, List<BookResponse> books) {
        return new AuthorWithBooksResponse(
                author.getId(), author.getName(), author.getEmail(), author.getCreatedAt(), books);
    }
}
