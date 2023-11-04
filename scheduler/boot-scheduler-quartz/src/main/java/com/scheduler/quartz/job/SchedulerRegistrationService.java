package com.scheduler.quartz.job;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SchedulerRegistrationService {

    @Bean
    JobDetail registerOddJob() {
        return newJob(SampleJob.class)
                .withIdentity("sample-job")
                .usingJobData("jobName", "odd")
                .storeDurably()
                .build();
    }

    @Bean
    Trigger triggerOddJob(JobDetail registerOddJob) {
        return newTrigger()
                .withIdentity("trigger-sample-job")
                .forJob(registerOddJob.getKey())
                .startAt(futureDate(5, DateBuilder.IntervalUnit.SECOND))
                .build();
    }
}
