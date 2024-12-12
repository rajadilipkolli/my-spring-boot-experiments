package com.example.keysetpagination.model.response;

import java.time.LocalDateTime;

/**
 * Response record representing an animal entity with its basic attributes and audit information.
 *
 * @param id Unique identifier of the animal
 * @param name Name of the animal
 * @param type Type/species of the animal
 * @param habitat Natural environment where the animal lives
 * @param created Timestamp when the animal record was created
 */
public record AnimalResponse(Long id, String name, String type, String habitat, LocalDateTime created) {}
