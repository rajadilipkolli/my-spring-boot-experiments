package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.model.common.Message;
import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.model.response.ScheduleJob;
import com.scheduler.quartz.service.JobsService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class JobsController {

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
    public Message saveOrUpdate(ScheduleJob job) {
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
    public Message pauseJob(ScheduleJob job) {
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

    @DeleteMapping(value = "/deleteJob")
    public Message deleteJob(ScheduleJob job) {
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
