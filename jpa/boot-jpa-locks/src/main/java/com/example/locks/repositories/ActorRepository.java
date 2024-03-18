package com.example.locks.repositories;

import com.example.locks.entities.Actor;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    @Query(value = "SELECT actor FROM Actor actor WHERE actor.actorId = :actorId")
    @QueryHints(
            value = {
                @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"),
                @QueryHint(name = "jakarta.persistence.query.timeout", value = "3000"),
            })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Actor> findByIdWithPessimisticWriteLock(@Param("actorId") Long actorId);

    @Query(value = "SELECT actor FROM Actor actor WHERE actor.actorId = :actorId")
    @QueryHints(
            value = {
                @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"),
                @QueryHint(name = "jakarta.persistence.query.timeout", value = "3000"),
            })
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Actor> findByIdWithPessimisticReadLock(@Param("actorId") Long actorId);
}
