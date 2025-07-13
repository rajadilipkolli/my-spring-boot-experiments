package com.example.jobrunr;

import com.example.jobrunr.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestJobRunrApplication {

    public static void main(String[] args) {
        SpringApplication.from(JobRunrApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
