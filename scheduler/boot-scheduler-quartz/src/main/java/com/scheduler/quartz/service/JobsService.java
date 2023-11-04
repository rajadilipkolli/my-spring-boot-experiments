package com.scheduler.quartz.service;

import com.scheduler.quartz.model.response.JobStatus;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;

@Service
public class JobsService {

    private final Scheduler scheduler;
    private final String groupName = "sample-group";

    public JobsService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public boolean deleteJob(String id) throws SchedulerException {
        JobKey jobKey = new JobKey(id, groupName);
        return scheduler.deleteJob(jobKey);
    }

    public List<String> getJobs() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).stream()
                .map(Key::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    public List<JobStatus> getJobsStatuses() throws SchedulerException {
        LinkedList<JobStatus> list = new LinkedList<>();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
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
}
