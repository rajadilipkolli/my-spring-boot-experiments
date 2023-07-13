package com.poc.boot.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.boot.rabbitmq.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestRabbitMQApplication.class)
@AutoConfigureMockMvc
class RabbitMQIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private RabbitTemplate rabbitTemplate;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertThat(this.rabbitTemplate).isNotNull();
    }

    @Test
    void testSendingMessage() throws Exception {
        Order order = new Order();
        order.setOrderNumber("1");
        order.setAmount(10d);
        order.setProductId("P1");
        mockMvc.perform(
                        post("/sendMsg")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("order", objectMapper.writeValueAsString(order)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "Order message sent successfully"));
    }
}
