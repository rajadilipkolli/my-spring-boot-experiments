package com.example.keysetpagination.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;

/**
 * Custom repository interface for efficient keyset pagination.
 *
 * @param <T> the domain type the repository manages
 * @see org.springframework.data.domain.Window
 */
public interface CustomRepository<T> {

    /**
     * Finds all entities matching the given specification using keyset pagination.
     *
     * @param spec The specification to filter entites
     * @param pageRequest The pagination information
     * @param scrollPosition The current position in the result set
     * @param entityClass The entity class on which operation should occur
     * @return A window containing the paginated results
     */
    Window<T> findAll(
            Specification<T> spec, PageRequest pageRequest, ScrollPosition scrollPosition, Class<T> entityClass);
}
