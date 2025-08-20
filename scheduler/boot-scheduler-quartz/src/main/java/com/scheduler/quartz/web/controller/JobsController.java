package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.model.common.Message;
import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.model.response.ScheduleJob;
import com.scheduler.quartz.service.JobsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JobsController {

    private static final Logger log = LoggerFactory.getLogger(JobsController.class);

    private final JobsService jobsService;

    public JobsController(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping
    List<ScheduleJob> getJobs() {
        return jobsService.getJobs();
    }

    @GetMapping("/statuses")
    List<JobStatus> getJobsStatuses() throws SchedulerException {
        return jobsService.getJobsStatuses();
    }

    @PostMapping(value = "/saveOrUpdate")
    public Message saveOrUpdate(@RequestBody @Valid ScheduleJob job) {
        log.info("saveOrUpdateJob  params : {}", job);
        Message message = Message.failure();
        try {
            jobsService.saveOrUpdate(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("updateCron ex:", e);
        }
        return message;
    }

    @PostMapping(value = "/pauseJob")
    public Message pauseJob(@RequestBody @Valid ScheduleJob job) {
        log.info("pauseJob params = {}", job);
        Message message = Message.failure();
        try {
            jobsService.pauseJob(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("pauseJob ex:", e);
        }
        return message;
    }

    @Operation(summary = "Resume a scheduled job")
    @ApiResponse(responseCode = "200", description = "Job resumed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid job parameters")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(value = "/resumeJob")
    public Message resumeJob(@RequestBody @Valid ScheduleJob job) {
        log.info("resumeJob params = {}", job);
        Message message = Message.failure();
        try {
            jobsService.resumeJob(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("resumeJob ex:", e);
        }
        return message;
    }

    @DeleteMapping(value = "/deleteJob")
    public Message deleteJob(@RequestBody @Valid ScheduleJob job) {
        log.info("deleteJob params : {}", job);
        Message message = Message.failure();
        try {
            jobsService.deleteJob(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("deleteJob ex:", e);
        }
        return message;
    }
}
