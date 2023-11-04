package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.service.JobsService;
import java.util.List;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobsController {

    private final JobsService jobsService;

    public JobsController(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping
    List<String> getJobs() throws SchedulerException {
        return jobsService.getJobs();
    }

    @GetMapping("/statuses")
    List<JobStatus> getJobsStatuses() throws SchedulerException {
        return jobsService.getJobsStatuses();
    }

    @DeleteMapping("/{id}")
    boolean deleteJob(@PathVariable String id) throws SchedulerException {
        return jobsService.deleteJob(id);
    }
}
