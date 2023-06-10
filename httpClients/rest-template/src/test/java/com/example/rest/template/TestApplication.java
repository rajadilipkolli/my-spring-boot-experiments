package com.example.rest.template;

import com.example.rest.template.common.MyContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@ImportTestcontainers(MyContainer.class)
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }
}
