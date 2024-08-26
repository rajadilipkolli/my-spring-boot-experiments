package com.learning.shedlock;

import com.learning.shedlock.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestShedLockApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.from(ShedLockApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
