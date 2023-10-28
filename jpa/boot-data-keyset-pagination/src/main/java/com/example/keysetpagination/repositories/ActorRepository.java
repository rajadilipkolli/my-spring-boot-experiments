package com.example.keysetpagination.repositories;

import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.example.keysetpagination.entities.Actor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    KeysetAwarePage<Actor> findAll(Specification<Actor> specification, Pageable pageable);
}
