package com.example.locks.repositories;

import com.example.locks.entities.Actor;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    @Query(value = "SELECT actor FROM Actor actor WHERE actor.actorId = ?1")
    @QueryHints(
            value = {
                @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"),
                @QueryHint(name = "jakarta.persistence.query.timeout", value = "3000"),
            })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Actor> findByIdWithPessimisticWriteLock(Long actorId);

    @Query(value = "SELECT actor FROM Actor actor WHERE actor.actorId = ?1")
    @QueryHints(
            value = {
                @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"),
                @QueryHint(name = "jakarta.persistence.query.timeout", value = "3000"),
            })
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Actor> findByIdWithPessimisticReadLock(Long actorId);
}
