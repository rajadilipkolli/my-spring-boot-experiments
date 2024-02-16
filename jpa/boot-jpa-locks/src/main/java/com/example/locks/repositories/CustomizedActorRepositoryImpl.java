package com.example.locks.repositories;

import com.example.locks.entities.Actor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@RequiredArgsConstructor
public class CustomizedActorRepositoryImpl implements CustomizedActorRepository {

    private static final String[] TIME_MEASURES = {"ms", "s", "min", "h", "d"};
    private static final TimeUnit[] TIME_UNITS = {
        TimeUnit.MILLISECONDS, TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS
    };

    private final EntityManager em;

    @Getter
    @Value("${concurrency.pessimisticLocking.requiredToSetLockTimeoutForTestsAtStartup: false}")
    private boolean requiredToSetLockTimeoutForTestsAtStartup;

    @Value("${concurrency.pessimisticLocking.requiredToSetLockTimeoutForEveryQuery: true}")
    private boolean requiredToSetLockTimeoutForEveryQuery;

    @Getter
    @Value("${concurrency.pessimisticLocking.requiredToSetLockTimeoutQueryHint: false}")
    private boolean requiredToSetLockTimeoutQueryHint;

    @Getter
    @Value("${concurrency.pessimisticLocking.delayAtTheEndOfTheQueryForPessimisticLockingTestingInMs: 0}")
    private long delayAtTheEndOfTheQueryForPessimisticLockingTestingInMs;

    @Getter
    @Value("${concurrency.pessimisticLocking.minimalPossibleLockTimeOutInMs: 1}")
    private long minimalPossibleLockTimeOutInMs;

    @Getter
    @Value("${concurrency.pessimisticLocking.lockTimeOutInMsForQueryGetItem: 5000}")
    private long lockTimeOutInMsForQueryGetItem;

    @Override
    public long getLockTimeout() {
        Query query = em.createNativeQuery("show lock_timeout");
        String result = (String) query.getSingleResult();
        return parseLockTimeOutToMilliseconds(result);
    }

    private long parseLockTimeOutToMilliseconds(String lockTimeOut) {
        for (int idx = 0; idx < TIME_MEASURES.length; idx++) {
            if (lockTimeOut.contains(TIME_MEASURES[idx])) {
                return Long.parseLong(lockTimeOut.substring(0, lockTimeOut.length() - TIME_MEASURES[idx].length()))
                        * TIME_UNITS[idx].toMillis(1);
            }
        }

        return Long.parseLong(lockTimeOut);
    }

    @Override
    public void setLockTimeout(long timeoutDurationInMs) {
        Query query = em.createNativeQuery("set local lock_timeout = " + timeoutDurationInMs);
        query.executeUpdate();
    }

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
        if (requiredToSetLockTimeoutForEveryQuery) {
            log.info("... set lockTimeOut {} ms through native query ...", getLockTimeOutInMsForQueryGetItem());
            setLockTimeout(getLockTimeOutInMsForQueryGetItem());
        }

        if (requiredToSetLockTimeoutQueryHint) {
            log.info("... set lockTimeOut {} ms through query hint ...", getLockTimeOutInMsForQueryGetItem());
            query.setHint("javax.persistence.lock.timeout", String.valueOf(getLockTimeOutInMsForQueryGetItem()));
        }

        return query;
    }
}
