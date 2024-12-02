package com.example.keysetpagination.services;

import com.example.keysetpagination.entities.Animal;
import org.springframework.data.jpa.domain.Specification;

public class AnimalSpecifications {

    public static Specification<Animal> hasName(String name) {
        return (root, query, criteriaBuilder) ->
                name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<Animal> hasType(String type) {
        return (root, query, criteriaBuilder) ->
                type == null ? null : criteriaBuilder.like(root.get("type"), "%" + type + "%");
    }
}
