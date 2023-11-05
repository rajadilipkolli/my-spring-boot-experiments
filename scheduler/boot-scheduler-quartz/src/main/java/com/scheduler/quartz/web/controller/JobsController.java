package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.model.common.Message;
import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.model.response.ScheduleJob;
import com.scheduler.quartz.service.JobsService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(
            value = "/saveOrUpdate",
            method = {RequestMethod.GET, RequestMethod.POST})
    public Message saveOrUpdate(ScheduleJob job) {
        log.info("params, job = {}", job);
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

    @RequestMapping(
            value = "/pauseJob",
            method = {RequestMethod.GET, RequestMethod.POST})
    public Message pauseJob(ScheduleJob job) {
        log.info("params, job = {}", job);
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

    @RequestMapping(
            value = "/deleteJob",
            method = {RequestMethod.GET, RequestMethod.POST})
    public Message deleteJob(ScheduleJob job) {
        log.info("params, job = {}", job);
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
