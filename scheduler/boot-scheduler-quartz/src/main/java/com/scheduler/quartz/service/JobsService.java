package com.scheduler.quartz.service;

import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import com.scheduler.quartz.job.SampleJob;
import com.scheduler.quartz.model.response.JobStatus;
import com.scheduler.quartz.model.response.ScheduleJob;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class JobsService {

    private final Scheduler scheduler;
    public static final String groupName = "sample-group";

    public JobsService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.jobName(), scheduleJob.jobGroup());
        scheduler.deleteJob(jobKey);
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
        for (JobKey jobKey : scheduler.getJobKeys(jobGroupEquals(groupName))) {
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
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.jobName(), groupName);
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
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.jobName(), groupName);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger != null) {
            throw new SchedulerException("job already exists!");
        }

        // simulate job info db persist operation
        ScheduleJob withJobId = scheduleJob.withJobId(String.valueOf(SampleJob.jobList.size() + 1));
        SampleJob.jobList.add(withJobId);

        JobDetail jobDetail = JobBuilder.newJob(SampleJob.class)
                .withIdentity(withJobId.jobName(), groupName)
                .storeDurably()
                .requestRecovery()
                .build();
        jobDetail.getJobDataMap().put("scheduleJob", withJobId.jobId());

        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(withJobId.cronExpression());
        trigger = TriggerBuilder.newTrigger()
                .withIdentity(withJobId.jobName() + "-trigger", groupName)
                .withSchedule(cronScheduleBuilder)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleJob.jobName(), scheduleJob.jobGroup());
        scheduler.pauseJob(jobKey);
    }
}
