package com.scheduler.quartz.model.response;

import jakarta.validation.constraints.NotBlank;

public record ScheduleJob(
        @NotBlank(message = "Job Name can't be blank") String jobName,
        String jobGroup,
        String jobStatus,
        String cronExpression,
        String desc) {}
