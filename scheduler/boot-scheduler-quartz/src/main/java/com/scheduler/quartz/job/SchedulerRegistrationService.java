package com.scheduler.quartz.job;

import static com.scheduler.quartz.service.JobsService.GROUP_NAME;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.scheduler.quartz.model.response.ScheduleJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SchedulerRegistrationService {

    @Bean
    JobDetail registerOddJob() {

        JobDetail jobDetail = JobBuilder.newJob(SampleJob.class)
                .withIdentity("oddEvenJob", GROUP_NAME)
                .withDescription("Sample OddEvenJob")
                .storeDurably()
                .requestRecovery()
                .build();
        String jobId = String.valueOf(SampleJob.JOB_LIST.size() + 1);
        ScheduleJob scheduleJob = new ScheduleJob(jobId, "oddEvenJob", GROUP_NAME, null, null, "Sample OddEvenJob");
        jobDetail.getJobDataMap().put("scheduleJob", jobId);
        SampleJob.JOB_LIST.add(scheduleJob);
        return jobDetail;
    }

    @Bean
    Trigger triggerOddJob(JobDetail registerOddJob) {
        return newTrigger()
                .withIdentity("sample-job-trigger", GROUP_NAME)
                .forJob(registerOddJob.getKey())
                .startAt(futureDate(10, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(120) // Run every 120 seconds
                        .repeatForever()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }
}
