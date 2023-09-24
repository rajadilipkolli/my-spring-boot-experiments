package com.example.rest.proxy;

import com.example.rest.proxy.config.DBTestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main)
                .with(DBTestContainersConfiguration.class)
                .run(args);
    }
}
