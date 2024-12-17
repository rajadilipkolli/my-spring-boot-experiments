package com.example.keysetpagination.repositories;

import com.example.keysetpagination.entities.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnimalRepository
        extends JpaRepository<Animal, Long>, CustomRepository<Animal>, JpaSpecificationExecutor<Animal> {}
