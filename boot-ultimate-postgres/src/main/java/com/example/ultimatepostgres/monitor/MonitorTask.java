package com.example.ultimatepostgres.monitor;

import com.example.ultimatepostgres.repository.CacheRepository;
import com.example.ultimatepostgres.repository.JobQueueRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MonitorTask {

    private static final Logger log = LoggerFactory.getLogger(MonitorTask.class);

    private final CacheRepository cacheRepository;
    private final JobQueueRepository jobQueueRepository;

    public MonitorTask(CacheRepository cacheRepository, JobQueueRepository jobQueueRepository) {
        this.cacheRepository = cacheRepository;
        this.jobQueueRepository = jobQueueRepository;
    }

    /**
     * Reports total cache rows, including expired entries, and jobs in all statuses.
     *
     * <p>Calls through the Spring proxy are skipped if the shared task lock is held. The lock
     * prevents concurrent execution only while its one-minute lease remains valid.
     *
     * @throws org.springframework.dao.DataAccessException if a repository count fails
     */
    @Scheduled(fixedDelay = 15000)
    @SchedulerLock(name = "monitorTask", lockAtLeastFor = "PT10S", lockAtMostFor = "PT1M")
    @Transactional(readOnly = true)
    public void monitor() {
        long cacheSize = cacheRepository.count();
        long totalJobs = jobQueueRepository.count();

        log.info("System Monitor: Cache Size = {}, Total Jobs = {}", cacheSize, totalJobs);
    }
}
