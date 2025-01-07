package com.example.bootbatchjpa.web.controllers;

import com.example.bootbatchjpa.config.logging.Loggable;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Loggable
@RequestMapping("/api/job")
class JobInvokerController {

    private final JobLauncher jobLauncher;

    // Declared in BatchConfig
    private final Job allCustomersJob;

    JobInvokerController(JobLauncher jobLauncher, Job allCustomersJob) {
        this.jobLauncher = jobLauncher;
        this.allCustomersJob = allCustomersJob;
    }

    @GetMapping("/customers")
    String allCustomersJobHandle(@RequestParam Long minId, @RequestParam Long maxId) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("minId", minId)
                .addLong("maxId", maxId)
                .toJobParameters();
        JobExecution jobExecution = this.jobLauncher.run(this.allCustomersJob, jobParameters);

        return "Batch job has been invoked as " + jobExecution.getJobId();
    }
}
