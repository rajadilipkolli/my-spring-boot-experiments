package com.example.ultimatepostgres.service;

import com.example.ultimatepostgres.repository.CacheRepository;
import java.time.OffsetDateTime;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CacheCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(CacheCleanupTask.class);

    private final CacheRepository cacheRepository;

    public CacheCleanupTask(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    /**
     * Deletes cache entries whose expiration is at or before the application clock's current time.
     *
     * <p>Calls through the Spring proxy are skipped if the shared task lock is held. The lock
     * prevents concurrent execution only while its five-minute lease remains valid.
     *
     * Note: UNLOGGED tables are not crash-safe.
     * DELETE produces dead rows that autovacuum reclaims.
     *
     * @throws org.springframework.dao.DataAccessException if deleting expired entries fails
     */
    @Scheduled(fixedDelayString = "${app.cache.cleanup-interval:60000}")
    // lockAtLeastFor must stay below app.cache.cleanup-interval
    @SchedulerLock(name = "cacheCleanupTask", lockAtLeastFor = "PT30S", lockAtMostFor = "PT5M")
    @Transactional
    public void cleanupExpiredEntries() {
        int deleted = cacheRepository.deleteExpired(OffsetDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired cache entries", deleted);
        }
    }
}
