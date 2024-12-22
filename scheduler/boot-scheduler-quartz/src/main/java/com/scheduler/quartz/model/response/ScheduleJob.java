package com.scheduler.quartz.model.response;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.With;

public record ScheduleJob(
        @With String jobId,
        @NotBlank(message = "Job Name cant be blank") String jobName,
        String jobGroup,
        String jobStatus,
        String cronExpression,
        String desc)
        implements Serializable {}
