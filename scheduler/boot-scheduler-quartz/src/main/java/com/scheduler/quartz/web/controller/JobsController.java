package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.model.common.Message;
import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.model.response.ScheduleJob;
import com.scheduler.quartz.service.JobsService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Please use actuator endpoint: POST /actuator/quartz/jobs/{group}/{name}/pause")
    @Deprecated(since = "3.5.0", forRemoval = true)
    @PostMapping(value = "/pauseJob")
    public Message pauseJob(@RequestBody @Valid ScheduleJob job) {
        log.warn(
                "This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/{}/{}/pause",
                job.jobGroup(),
                job.jobName());
        return Message.failure("This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/"
                + job.jobGroup() + "/" + job.jobName() + "/pause");
    }

    @Operation(summary = "Please use actuator endpoint: POST /actuator/quartz/jobs/{group}/{name}/resume")
    @Deprecated(since = "3.5.0", forRemoval = true)
    @PostMapping(value = "/resumeJob")
    public Message resumeJob(@RequestBody @Valid ScheduleJob job) {
        log.warn(
                "This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/{}/{}/resume",
                job.jobGroup(),
                job.jobName());
        return Message.failure("This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/"
                + job.jobGroup() + "/" + job.jobName() + "/resume");
    }

    @Operation(summary = "Please use actuator endpoint: POST /actuator/quartz/jobs/{group}/{name}/trigger")
    @Deprecated(since = "3.5.0", forRemoval = true)
    @PostMapping(value = "/runJob")
    public Message runJob(@RequestBody @Valid ScheduleJob job) {
        log.warn(
                "This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/{}/{}/trigger",
                job.jobGroup(),
                job.jobName());
        return Message.failure("This endpoint is deprecated. Please use actuator endpoint: POST /actuator/quartz/jobs/"
                + job.jobGroup() + "/" + job.jobName() + "/trigger");
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
