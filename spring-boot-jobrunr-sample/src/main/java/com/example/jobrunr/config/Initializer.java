package com.example.jobrunr.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Initializer implements CommandLineRunner {

    private final JobScheduler jobScheduler;

    @Override
    public void run(String... args) {
        log.info("Running Initializer.....");
        jobScheduler.enqueue(this::doSomeWork);
        log.info("Completed setting Job using jobScheduler");
        BackgroundJob.enqueue(UUID.randomUUID(), this::doSomeWork);
        log.info("Completed setting BackgroundJob");
        BackgroundJob.schedule(Instant.now().plusMillis(1), this::doSomeWork);
        log.info("Completed Scheduling BackgroundJob");
        BackgroundJob.scheduleRecurrently(Cron.every15seconds(), this::doSomeWork);
        log.info("Completed Scheduling BackgroundJob Recurrently");
        BackgroundJob.scheduleRecurrently(Cron.every30seconds(), this::doWorkWithCustomJobFilters);
        log.info("Completed Scheduling BackgroundJob Recurrently with 2 reties");
    }

    @Job(name = "doSomeWork")
    public void doSomeWork() {
        log.info("Hi, I am from BackgroundJob scheduling at {}", LocalDateTime.now());
    }

    @Job(name = "someJobName", retries = 2)
    public void doWorkWithCustomJobFilters() {
        log.info("Hi, I will be retried only twice ");
    }
}
