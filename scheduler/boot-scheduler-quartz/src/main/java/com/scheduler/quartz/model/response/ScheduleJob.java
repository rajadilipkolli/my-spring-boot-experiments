package com.scheduler.quartz.model.response;

import jakarta.validation.constraints.NotBlank;
import lombok.With;

public record ScheduleJob(
        @With String jobId,
        @NotBlank(message = "Job Name can't be blank") String jobName,
        String jobGroup,
        String jobStatus,
        String cronExpression,
        String desc) {}
