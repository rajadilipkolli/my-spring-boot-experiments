package com.example.ultimatepostgres.service;

import com.example.ultimatepostgres.model.JobQueueEntity;
import com.example.ultimatepostgres.repository.JobQueueRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
public class JobProducerService {

    private final JobQueueRepository jobQueueRepository;

    public JobProducerService(JobQueueRepository jobQueueRepository) {
        this.jobQueueRepository = jobQueueRepository;
    }

    @Transactional
    public JobQueueEntity enqueue(JsonNode payload, int priority) {
        JobQueueEntity job = new JobQueueEntity();
        job.setPayload(payload);
        job.setStatus(com.example.ultimatepostgres.model.JobStatus.PENDING);
        job.setAvailableAt(OffsetDateTime.now());
        job.setPriority(priority);
        return jobQueueRepository.save(job);
    }
}
