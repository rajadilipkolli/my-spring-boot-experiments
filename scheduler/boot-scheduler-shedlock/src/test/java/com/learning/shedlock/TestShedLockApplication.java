package com.learning.shedlock;

import com.learning.shedlock.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestShedLockApplication {

    public static void main(String[] args) {
        SpringApplication.from(ShedLockApplication::main)
                .with(ContainersConfig.class)
                .withAdditionalProfiles("local")
                .run(args);
    }
}
