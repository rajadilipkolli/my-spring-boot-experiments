package com.example.jooq.spring.model.response;

import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import java.time.OffsetDateTime;

public record AuthorResponse(Long id, String name, String email, OffsetDateTime createdAt) {

    /** Copies author record values into an API response. */
    public static AuthorResponse toResponse(AuthorsRecord record) {
        return new AuthorResponse(record.getId(), record.getName(), record.getEmail(), record.getCreatedAt());
    }
}
