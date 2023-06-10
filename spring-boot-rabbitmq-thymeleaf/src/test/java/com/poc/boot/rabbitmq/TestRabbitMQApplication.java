package com.poc.boot.rabbitmq;

import com.poc.boot.rabbitmq.config.MyTestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestRabbitMQApplication {

    public static void main(String[] args) {
        SpringApplication.from(RabbitMQApplication::main)
                .with(MyTestContainersConfiguration.class)
                .run();
    }
}
