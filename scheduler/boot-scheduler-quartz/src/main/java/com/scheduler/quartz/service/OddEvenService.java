package com.scheduler.quartz.service;

import org.springframework.stereotype.Service;

@Service
public class OddEvenService {

    public void execute(String jobName) {
        log.info("JobName :{}", jobName);
    }
}
