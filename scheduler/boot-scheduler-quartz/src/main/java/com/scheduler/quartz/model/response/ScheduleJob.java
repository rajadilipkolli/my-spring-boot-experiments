package com.scheduler.quartz.model.response;

import java.io.Serializable;
import lombok.With;

public record ScheduleJob(
        @With String jobId, String jobName, String jobGroup, String jobStatus, String cronExpression, String desc)
        implements Serializable {}
