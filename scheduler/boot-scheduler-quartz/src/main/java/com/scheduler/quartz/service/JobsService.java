package com.scheduler.quartz.service;

import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import com.scheduler.quartz.job.SampleJob;
import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.model.response.ScheduleJob;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JobsService {

    private final Scheduler scheduler;
    public static final String GROUP_NAME = "sample-group";

    public JobsService(SchedulerFactoryBean schedulerFactoryBean) {
        this.scheduler = schedulerFactoryBean.getScheduler();
    }

    public List<ScheduleJob> getJobs() {
        List<ScheduleJob> jobList = new ArrayList<>();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeySet = scheduler.getJobKeys(matcher);
            for (JobKey jobKey : jobKeySet) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    String cronExpression = null;
                    if (trigger instanceof CronTrigger cronTrigger) {
                        cronExpression = cronTrigger.getCronExpression();
                    }
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    String jobId = (String) jobDetail.getJobDataMap().get("scheduleJob");
                    ScheduleJob scheduleJob = new ScheduleJob(
                            jobId, jobKey.getName(), jobKey.getGroup(), triggerState.name(), cronExpression, null);
                    jobList.add(scheduleJob);
                }
            }
        } catch (SchedulerException e) {
            log.error("SchedulerException occurred ", e);
        }
        return jobList;
    }

    public List<JobStatus> getJobsStatuses() throws SchedulerException {
        LinkedList<JobStatus> list = new LinkedList<>();
        for (JobKey jobKey : scheduler.getJobKeys(jobGroupEquals(GROUP_NAME))) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            for (Trigger trigger : triggers) {
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                if (Trigger.TriggerState.COMPLETE.equals(triggerState)) {
                    list.add(new JobStatus(jobKey.getName(), true));
                } else {
                    list.add(new JobStatus(jobKey.getName(), false));
                }
            }
        }
        list.sort(Comparator.comparing(JobStatus::jobName));
        return list;
    }

    public void saveOrUpdate(ScheduleJob scheduleJob) throws SchedulerException {
        if (!StringUtils.hasText(scheduleJob.jobId())) {
            addJob(scheduleJob);
        } else {
            updateJobCronExpression(scheduleJob);
        }
    }

    private void updateJobCronExpression(ScheduleJob scheduleJob) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.jobName(), GROUP_NAME);
        CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.cronExpression());
        if (cronTrigger == null) {
            cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(cronScheduleBuilder)
                    .build();
        } else {
            cronTrigger = cronTrigger
                    .getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .withSchedule(cronScheduleBuilder)
                    .build();
        }
        scheduler.rescheduleJob(triggerKey, cronTrigger);
    }

    private void addJob(ScheduleJob scheduleJob) throws SchedulerException {
        // Create TriggerKey for the job
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.jobName(), GROUP_NAME);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        // Throw exception if the job already exists
        if (trigger != null) {
            throw new SchedulerException(
                    "Job already exists with name '" + scheduleJob.jobName() + "' in group '" + GROUP_NAME + "'");
        }

        // simulate job info db persist operation
        ScheduleJob withJobId = scheduleJob.withJobId(UUID.randomUUID().toString());

        // Build the JobDetail with recovery and durability
        JobDetail jobDetail = JobBuilder.newJob(SampleJob.class)
                .withIdentity(withJobId.jobName(), GROUP_NAME)
                .withDescription(
                        StringUtils.hasText(scheduleJob.desc()) ? scheduleJob.desc() : "No description provided")
                .storeDurably()
                .requestRecovery()
                .build();
        jobDetail.getJobDataMap().put("scheduleJob", withJobId.jobId());

        // Build the Trigger with Cron expression and associate it with the job
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(withJobId.cronExpression());
        trigger = TriggerBuilder.newTrigger()
                .withIdentity(withJobId.jobName() + "-trigger", GROUP_NAME)
                .withDescription(
                        StringUtils.hasText(scheduleJob.desc()) ? scheduleJob.desc() : "No description provided")
                .withSchedule(cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        JobKey jobKey = JobKey.jobKey(scheduleJob.jobName(), scheduleJob.jobGroup());
        log.info("Scheduled job with key: {}", jobKey);
    }

    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.jobName(), scheduleJob.jobGroup());
        validateJobExists(jobKey);
        scheduler.pauseJob(jobKey);
        log.info("Paused job with key: {}", jobKey);
    }

    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.jobName(), scheduleJob.jobGroup());
        validateJobExists(jobKey);
        scheduler.resumeJob(jobKey);
        log.info("Resumed job with key: {}", jobKey);
    }

    public void runJob(ScheduleJob job) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(job.jobName(), job.jobGroup());
        validateJobExists(jobKey);
        scheduler.triggerJob(jobKey);
        log.info("Triggered job with key: {}", jobKey);
    }

    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.jobName(), scheduleJob.jobGroup());
        validateJobExists(jobKey);
        scheduler.deleteJob(jobKey);
        log.info("Deleted job with key: {}", jobKey);
    }

    private void validateJobExists(JobKey jobKey) throws SchedulerException {
        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Job does not exist with key: " + jobKey);
        }
    }
}
