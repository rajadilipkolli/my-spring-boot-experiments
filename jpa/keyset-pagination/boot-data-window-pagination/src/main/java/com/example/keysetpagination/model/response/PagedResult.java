package com.example.keysetpagination.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Constructs a PagedResult from a Spring Data Page object.
 * Note: Converts zero-based page numbering to one-based for client consumption.
 *
 * @param page The Spring Data Page object
 * @param data The list of converted data items
 * @param <R> The type of the source Page
 * @throws IllegalArgumentException if page or data is null
 */
public record PagedResult<T>(
        List<T> data,
        long totalElements,
        int pageNumber,
        int totalPages,
        @JsonProperty("isFirst") boolean isFirst,
        @JsonProperty("isLast") boolean isLast,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("hasPrevious") boolean hasPrevious) {
    public <R> PagedResult(Page<R> page, List<T> data) {
        this(
                data,
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());
    }
}
