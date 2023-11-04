package com.scheduler.quartz.job;

import com.scheduler.quartz.service.OddEvenService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class SampleJob implements Job {

    private final OddEvenService oddEvenService;

    public SampleJob(OddEvenService oddEvenService) {
        this.oddEvenService = oddEvenService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String jobName = jobExecutionContext.getJobDetail().getJobDataMap().getString("jobName");
        oddEvenService.execute(jobName);
    }
}
