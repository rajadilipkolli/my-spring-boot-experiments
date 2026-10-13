package com.example.jooq.spring.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "ISBN is required") String isbn,
        @NotNull(message = "Author ID is required") Long authorId,
        Integer publishedYear) {}
