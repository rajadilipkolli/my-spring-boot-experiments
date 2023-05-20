package com.example.graphql;

import com.example.graphql.config.MyContainersConfiguration;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(MyContainersConfiguration.class).run(args);
    }
}
