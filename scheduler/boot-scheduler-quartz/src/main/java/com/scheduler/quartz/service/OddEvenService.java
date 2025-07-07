package com.scheduler.quartz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OddEvenService {
    private static final Logger log = LoggerFactory.getLogger(OddEvenService.class);

    public void execute(String jobName) {
        log.info("JobName :{}", jobName);
    }
}
