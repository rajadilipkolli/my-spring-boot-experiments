package com.scheduler.quartz.model.response;

import jakarta.validation.constraints.NotBlank;

public record ScheduleJob(
        String jobId,
        @NotBlank(message = "Job Name can't be blank") String jobName,
        String jobGroup,
        String jobStatus,
        String cronExpression,
        String desc) {
    public ScheduleJob withJobId(final String jobId) {
        return this.jobId == jobId
                ? this
                : new ScheduleJob(jobId, this.jobName, this.jobGroup, this.jobStatus, this.cronExpression, this.desc);
    }
}
