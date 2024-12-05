package com.example.restdocs;

import com.example.restdocs.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(ContainerConfig.class).run(args);
    }
}
