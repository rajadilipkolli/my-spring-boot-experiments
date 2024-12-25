package com.example.hibernatecache;

import com.example.hibernatecache.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestHibernate2ndLevelCacheApplication {

    public static void main(String[] args) {
        SpringApplication.from(Hibernate2ndLevelCacheApplication::main)
                .with(ContainersConfig.class)
                .withAdditionalProfiles("local")
                .run(args);
    }
}
