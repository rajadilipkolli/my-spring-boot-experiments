package com.example.locks;

import com.example.locks.common.ContainersConfig;
import com.example.locks.utils.AppConstants;
import org.springframework.boot.SpringApplication;

public class TestJpaLocksApplication {

    public static void main(String[] args) {
        SpringApplication.from(JpaLocksApplication::main)
                .with(ContainersConfig.class)
                .withAdditionalProfiles(AppConstants.PROFILE_LOCAL)
                .run(args);
    }
}
