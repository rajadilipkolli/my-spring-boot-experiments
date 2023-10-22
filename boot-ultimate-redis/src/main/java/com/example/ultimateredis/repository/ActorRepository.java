package com.example.ultimateredis.repository;

import com.example.ultimateredis.model.Actor;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends CrudRepository<Actor, String> {
    Optional<Actor> findByName(String name);

    Optional<Actor> findByNameAndAge(String name, Integer age);

    void deleteByName(String name);
}
