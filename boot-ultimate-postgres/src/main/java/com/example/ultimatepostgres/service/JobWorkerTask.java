package com.example.ultimatepostgres.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobWorkerTask {

    private static final Logger log = LoggerFactory.getLogger(JobWorkerTask.class);

    private final JobConsumerService jobConsumerService;

    public JobWorkerTask(JobConsumerService jobConsumerService) {
        this.jobConsumerService = jobConsumerService;
    }

    @Scheduled(fixedDelayString = "${app.queue.worker-interval:5000}")
    public void processJobs() {
        int processedCount = jobConsumerService.claimAndProcess(10);
        if (processedCount > 0) {
            log.info("Worker processed {} jobs", processedCount);
        }
    }
}
