package com.example.multitenancy.partition;

import com.example.multitenancy.partition.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestMultiTenancyPartitionApplication {

    public static void main(String[] args) {
        SpringApplication.from(MultiTenancyPartitionApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
