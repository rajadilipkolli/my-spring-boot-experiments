package com.poc.boot.rabbitmq.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.service.OrderMessageSender;
import com.poc.boot.rabbitmq.util.MockObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OrderMessageSender orderMessageSender;

    @Test
    void handleMessage() throws Exception {

        willDoNothing().given(this.orderMessageSender).sendOrder(MockObjectCreator.getOrder());

        this.mockMvc
                .perform(
                        post("/sendMsg")
                                .flashAttr("order", MockObjectCreator.getOrder())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(flash().attribute("message", "Order message sent successfully"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void handleMessageThrowsException() throws Exception {
        willThrow(new RuntimeException("Exception") {})
                .given(this.orderMessageSender)
                .sendOrder(any(Order.class));

        this.mockMvc
                .perform(
                        post("/sendMsg")
                                .flashAttr("order", MockObjectCreator.getOrder())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(
                        jsonPath("$.type")
                                .value("https://api.boot-rabbitmq-thymeleaf.com/errors/exception"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Unable To Parse Order[orderNumber=1, productId=P1, amount=10.0]"))
                .andExpect(jsonPath("$.instance").value("/sendMsg"));
    }
}
