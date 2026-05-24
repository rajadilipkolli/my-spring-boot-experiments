package com.example.bootbatchjpa.web.controllers;

import com.example.bootbatchjpa.config.logging.Loggable;
import java.util.UUID;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Loggable
@RequestMapping("/api/job")
class JobInvokerController {

    private final JobOperator jobOperator;

    // Declared in BatchConfig
    private final Job allCustomersJob;

    JobInvokerController(JobOperator jobOperator, Job allCustomersJob) {
        this.jobOperator = jobOperator;
        this.allCustomersJob = allCustomersJob;
    }

    @GetMapping("/customers")
    GenericMessage allCustomersJobHandle() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();
        JobExecution jobExecution = this.jobOperator.start(this.allCustomersJob, jobParameters);

        return new GenericMessage("Batch job has been invoked as " + jobExecution.getJobInstanceId());
    }

    private record GenericMessage(String message) {}
}
