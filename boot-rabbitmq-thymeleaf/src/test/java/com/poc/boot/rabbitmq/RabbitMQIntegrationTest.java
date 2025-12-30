package com.poc.boot.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.poc.boot.rabbitmq.common.ContainerConfiguration;
import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.repository.TrackingStateRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ContainerConfiguration.class)
@AutoConfigureMockMvc
class RabbitMQIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private RabbitTemplate rabbitTemplate;

    @Autowired private TrackingStateRepository trackingStateRepository;

    @Test
    void contextLoads() {
        assertThat(this.rabbitTemplate).isNotNull();
        assertThat(rabbitTemplate.getExchange()).isEmpty();
        assertThat(rabbitTemplate.getRoutingKey()).isEmpty();
    }

    @Test
    void sendingMessage() throws Exception {
        Order order = new Order("1", "P1", 10d);
        long count = trackingStateRepository.countByStatus("processed");
        mockMvc.perform(
                        post("/sendMsg")
                                .flashAttr("order", order)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "Order message sent successfully"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> {
                            long afterCount = trackingStateRepository.countByStatus("processed");
                            assertThat(afterCount).isEqualTo(count + 1);
                        });
    }

    @Test
    void sendingFailedMessage() throws Exception {
        long count = trackingStateRepository.countByStatus("processed");
        Order order = new Order("2", "P2", -10d);
        mockMvc.perform(post("/sendMsg").flashAttr("order", order))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "Order message sent successfully"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> {
                            long afterCount = trackingStateRepository.countByStatus("processed");
                            assertThat(afterCount).isEqualTo(count + 1);
                        });
        await().pollDelay(2, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> {
                            long afterCount = trackingStateRepository.countByStatus("failed");
                            assertThat(afterCount).isOne();
                            afterCount = trackingStateRepository.countByStatus("processed");
                            assertThat(afterCount).isEqualTo(count);
                        });
    }
}
