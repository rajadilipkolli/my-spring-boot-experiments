package com.example.jooq.spring.model.response;

import com.example.jooq.spring.db.tables.records.BooksRecord;
import java.time.LocalDateTime;

public record BookResponse(
        Long id, String title, String isbn, Long authorId, Integer publishedYear, LocalDateTime createdAt) {

    public static BookResponse toBookResponse(BooksRecord record) {
        return new BookResponse(
                record.getId(),
                record.getTitle(),
                record.getIsbn(),
                record.getAuthorId(),
                record.getPublishedYear(),
                record.getCreatedAt());
    }
}
