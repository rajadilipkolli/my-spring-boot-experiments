package com.example.ultimatepostgres.service;

import com.example.ultimatepostgres.repository.CacheRepository;
import java.time.OffsetDateTime;
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
     * Note: UNLOGGED tables are not crash-safe.
     * DELETE produces dead rows that autovacuum reclaims.
     */
    @Scheduled(fixedDelayString = "${app.cache.cleanup-interval:60000}")
    @Transactional
    public void cleanupExpiredEntries() {
        int deleted = cacheRepository.deleteExpired(OffsetDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired cache entries", deleted);
        }
    }
}
