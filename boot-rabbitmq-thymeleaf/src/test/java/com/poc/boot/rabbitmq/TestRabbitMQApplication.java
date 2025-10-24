package com.poc.boot.rabbitmq;

import com.poc.boot.rabbitmq.common.ContainerConfiguration;
import org.springframework.boot.SpringApplication;

public class TestRabbitMQApplication {

    static void main(String[] args) {
        SpringApplication.from(RabbitMQApplication::main).with(ContainerConfiguration.class).run();
    }
}
