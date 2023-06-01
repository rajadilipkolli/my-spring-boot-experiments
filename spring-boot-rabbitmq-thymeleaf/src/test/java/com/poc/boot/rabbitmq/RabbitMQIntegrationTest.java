package com.poc.boot.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;

import com.poc.boot.rabbitmq.config.MyTestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MyTestContainersConfiguration.class)
class RabbitMQIntegrationTest {

    @Autowired private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        assertThat(this.rabbitTemplate).isNotNull();
    }
}
