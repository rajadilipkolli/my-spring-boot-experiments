package com.poc.boot.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestRabbitMQApplication {

    @Bean
    @ServiceConnection
    @RestartScope
    RabbitMQContainer rabbitMQContainer() {
        return new RabbitMQContainer(
                DockerImageName.parse("rabbitmq").withTag("3.12.12-management"));
    }

    public static void main(String[] args) {
        SpringApplication.from(RabbitMQApplication::main).with(TestRabbitMQApplication.class).run();
    }
}
