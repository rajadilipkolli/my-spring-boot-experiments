package com.example.keysetpagination.repositories;

import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.example.keysetpagination.entities.Actor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.ListCrudRepository;

public interface ActorRepository extends ListCrudRepository<Actor, Long> {

    KeysetAwarePage<Actor> findAll(Specification<Actor> specification, KeysetPageable pageable);

    KeysetAwarePage<Actor> findAll(KeysetPageable keysetPageable);
}
