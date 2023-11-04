package com.scheduler.quartz.job;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SchedulerRegistrationService {

    @Bean
    JobDetail registerOddJob() {
        return newJob(SampleJob.class)
                .withIdentity("sample-job", "sample-group")
                .usingJobData("jobName", "odd")
                .storeDurably()
                .requestRecovery()
                .build();
    }

    @Bean
    Trigger triggerOddJob(JobDetail registerOddJob) {
        return newTrigger()
                .withIdentity("trigger-sample-job", "sample-group")
                .forJob(registerOddJob.getKey())
                .startAt(futureDate(10, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(5) // Run every 2 seconds
                        .repeatForever()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }
}
