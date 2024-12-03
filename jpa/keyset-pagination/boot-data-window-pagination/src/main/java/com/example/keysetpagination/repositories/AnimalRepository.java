package com.example.keysetpagination.repositories;

import com.example.keysetpagination.entities.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long>, CustomRepository<Animal> {}
