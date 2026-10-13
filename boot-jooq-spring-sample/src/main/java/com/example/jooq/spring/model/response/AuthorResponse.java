package com.example.jooq.spring.model.response;

import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import java.time.LocalDateTime;

public record AuthorResponse(Long id, String name, String email, LocalDateTime createdAt) {

    public static AuthorResponse toResponse(AuthorsRecord record) {
        return new AuthorResponse(record.getId(), record.getName(), record.getEmail(), record.getCreatedAt());
    }
}
