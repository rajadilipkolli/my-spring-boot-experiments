package com.example.keysetpagination.repositories;

import com.example.keysetpagination.entities.Animal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;

public interface CustomAnimalRepository {
    Window<Animal> findAll(Specification<Animal> spec, PageRequest pageRequest, ScrollPosition scrollPosition);
}
