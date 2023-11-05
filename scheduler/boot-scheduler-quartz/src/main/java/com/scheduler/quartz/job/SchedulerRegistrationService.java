package com.scheduler.quartz.job;

import static com.scheduler.quartz.service.JobsService.groupName;
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
                .withIdentity("oddEvenJob", groupName)
                .withDescription("Sample OddEvenJob")
                .storeDurably()
                .requestRecovery()
                .build();
        String jobId = String.valueOf(SampleJob.jobList.size() + 1);
        ScheduleJob scheduleJob = new ScheduleJob(jobId, "oddEvenJob", groupName, null, null, "Sample OddEvenJob");
        jobDetail.getJobDataMap().put("scheduleJob", jobId);
        SampleJob.jobList.add(scheduleJob);
        return jobDetail;
    }

    @Bean
    Trigger triggerOddJob(JobDetail registerOddJob) {
        return newTrigger()
                .withIdentity("sample-job-trigger", groupName)
                .forJob(registerOddJob.getKey())
                .startAt(futureDate(10, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(60) // Run every 2 seconds
                        .repeatForever()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }
}
