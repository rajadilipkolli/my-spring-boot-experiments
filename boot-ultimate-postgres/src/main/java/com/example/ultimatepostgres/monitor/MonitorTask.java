package com.example.ultimatepostgres.monitor;

import com.example.ultimatepostgres.repository.CacheRepository;
import com.example.ultimatepostgres.repository.JobQueueRepository;
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

    @Scheduled(fixedDelay = 15000)
    @Transactional(readOnly = true)
    public void monitor() {
        long cacheSize = cacheRepository.count();
        long totalJobs = jobQueueRepository.count();

        log.info("System Monitor: Cache Size = {}, Total Jobs = {}", cacheSize, totalJobs);
    }
}
