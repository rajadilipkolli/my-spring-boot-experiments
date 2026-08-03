package com.example.ultimatepostgres.web.controller;

import com.example.ultimatepostgres.model.JobQueueEntity;
import com.example.ultimatepostgres.model.JobRequest;
import com.example.ultimatepostgres.repository.JobQueueRepository;
import com.example.ultimatepostgres.service.JobProducerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class JobQueueController {

    private final JobProducerService jobProducerService;
    private final JobQueueRepository jobQueueRepository;

    public JobQueueController(JobProducerService jobProducerService, JobQueueRepository jobQueueRepository) {
        this.jobProducerService = jobProducerService;
        this.jobQueueRepository = jobQueueRepository;
    }

    @PostMapping
    public ResponseEntity<JobQueueEntity> enqueue(@Valid @RequestBody JobRequest request) {
        JobQueueEntity job = jobProducerService.enqueue(
                request.getPayload(), request.getPriority() != null ? request.getPriority() : 0);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/status")
    public ResponseEntity<List<JobQueueEntity>> getStatus() {
        return ResponseEntity.ok(jobQueueRepository.findTop100ByOrderByCreatedAtDesc());
    }
}
