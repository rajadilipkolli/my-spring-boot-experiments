package com.example.keysetpagination.repositories;

import com.example.keysetpagination.entities.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorRepository extends JpaRepository<Actor, Long>, CustomActorRepository {}
