package com.example.ultimatepostgres.service;

import com.example.ultimatepostgres.model.JobQueueEntity;
import com.example.ultimatepostgres.repository.JobQueueRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobConsumerService {

    private static final Logger log = LoggerFactory.getLogger(JobConsumerService.class);

    private final JobQueueRepository jobQueueRepository;

    public JobConsumerService(JobQueueRepository jobQueueRepository) {
        this.jobQueueRepository = jobQueueRepository;
    }

    @Transactional
    public int claimAndProcess(int batchSize) {
        List<JobQueueEntity> jobs = jobQueueRepository.claimJobs(OffsetDateTime.now(), batchSize);

        if (jobs.isEmpty()) {
            return 0;
        }

        List<Long> jobIds = jobs.stream().map(JobQueueEntity::getId).toList();
        jobQueueRepository.markInProgress(jobIds);

        int processedCount = 0;
        for (JobQueueEntity job : jobs) {
            try {
                processJob(job);
                jobQueueRepository.markCompleted(job.getId());
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to process job {}", job.getId(), e);
                if (job.getAttemptCount() < 3) {
                    // Exponential backoff: 5s, 25s, 125s
                    long backoffSeconds = (long) Math.pow(5, job.getAttemptCount() + 1);
                    jobQueueRepository.scheduleRetry(
                            job.getId(), OffsetDateTime.now().plusSeconds(backoffSeconds));
                    log.info("Job {} scheduled for retry in {} seconds", job.getId(), backoffSeconds);
                } else {
                    jobQueueRepository.markFailed(job.getId());
                    log.error("Job {} exhausted retries and is marked FAILED", job.getId());
                }
            }
        }

        return processedCount;
    }

    private void processJob(JobQueueEntity job) {
        log.info("Processing job id={}, payload={}", job.getId(), job.getPayload());
        // Simulate some fast processing
    }
}
