package com.learning.shedlock.config;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Initializer {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    @Scheduled(cron = "0 */1 * * * *")
    @SchedulerLock(name = "lockedTask", lockAtMostFor = "PT5S", lockAtLeastFor = "PT2S")
    public void lockedTask() {
        // code for task to be performed
        log.info("Scheduler Job triggered");
    }
}
