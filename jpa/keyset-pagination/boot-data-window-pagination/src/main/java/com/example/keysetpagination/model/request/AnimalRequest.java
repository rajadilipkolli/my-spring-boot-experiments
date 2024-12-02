package com.example.keysetpagination.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request object for animal operations containing essential animal information.
 *
 * @param name    The name of the animal (required)
 * @param type    The type/species of the animal (required)
 * @param habitat The natural environment where the animal lives (optional)
 */
public record AnimalRequest(
        @NotBlank(message = "Name cannot be blank") String name,
        @NotBlank(message = "Type cannot be blank") String type,
        String habitat) {}
