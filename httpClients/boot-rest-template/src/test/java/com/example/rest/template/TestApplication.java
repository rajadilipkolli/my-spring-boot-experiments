package com.example.rest.template;

import com.example.rest.template.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(BatchApplication::main).with(ContainersConfig.class).run(args);
    }
}
