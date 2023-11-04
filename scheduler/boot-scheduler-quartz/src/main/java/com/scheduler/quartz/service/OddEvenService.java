package com.scheduler.quartz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OddEvenService {

    public void execute(String jobName) {
        log.info("JobName :{}", jobName);
    }
}
