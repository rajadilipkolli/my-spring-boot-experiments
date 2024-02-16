package com.example.locks.repositories;

import com.example.locks.entities.Actor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomizedActorRepositoryImpl implements CustomizedActorRepository {

    private final EntityManager em;

    @Override
    public long getLockTimeout() {
        return 0;
    }

    @Override
    public void setLockTimeout(long timeoutDurationInMs) {}

    @Override
    public Actor getActorAndObtainPessimisticWriteLockingOnItById(Long id) {
        log.info("Trying to obtain pessimistic lock ...");

        Query query = em.createQuery("select actor from Actor actor where actor.id = :id");
        query.setParameter("id", id);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query = setLockTimeoutIfRequired(query);
        Actor actor = (Actor) query.getSingleResult();

        log.info("... pessimistic lock obtained by request from ...");

        insertArtificialDelayAtTheEndOfTheQueryForTestsOnly();

        log.info("... pessimistic lock released.");

        return actor;
    }

    protected void insertArtificialDelayAtTheEndOfTheQueryForTestsOnly() {
        // for testing purposes only
        try {
            TimeUnit.MILLISECONDS.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    protected Query setLockTimeoutIfRequired(Query query) {
        Query timeoutQuery = em.createNativeQuery("set local lock_timeout = " + 5000);
        timeoutQuery.executeUpdate();

        return query;
    }
}
