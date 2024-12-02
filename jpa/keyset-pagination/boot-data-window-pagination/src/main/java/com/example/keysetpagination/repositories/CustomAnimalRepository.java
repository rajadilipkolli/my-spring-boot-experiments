package com.example.keysetpagination.repositories;

import com.example.keysetpagination.entities.Animal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;

/**
 * Custom repository interface for efficient keyset pagination of Animal entities.
 */
public interface CustomAnimalRepository {

    /**
     * Finds all animals matching the given specification using keyset pagination.
     *
     * @param spec The specification to filter animals
     * @param pageRequest The pagination information
     * @param scrollPosition The current position in the result set
     * @return A window containing the paginated results
     */
    Window<Animal> findAll(Specification<Animal> spec, PageRequest pageRequest, ScrollPosition scrollPosition);
}
