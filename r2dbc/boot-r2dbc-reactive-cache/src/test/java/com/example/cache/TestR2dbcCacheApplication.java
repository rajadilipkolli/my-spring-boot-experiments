package com.example.cache;

import com.example.cache.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestR2dbcCacheApplication {

    public static void main(String[] args) {
        SpringApplication.from(R2dbcCacheApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
