package com.poc.boot.rabbitmq.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.service.OrderMessageSender;
import com.poc.boot.rabbitmq.util.MockObjectCreator;
import java.io.Serial;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderMessageSender orderMessageSender;

    @Test
    void testHandleMessage() throws Exception {

        willDoNothing().given(this.orderMessageSender).sendOrder(MockObjectCreator.getOrder());

        this.mockMvc
                .perform(
                        post("/sendMsg")
                                .content(
                                        this.objectMapper.writeValueAsString(
                                                MockObjectCreator.getOrder()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute("message", "Order message sent successfully"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testHandleMessageThrowsException() throws Exception {
        willThrow(
                        new JsonProcessingException("Exception") {
                            @Serial private static final long serialVersionUID = 1L;
                        })
                .given(this.orderMessageSender)
                .sendOrder(any(Order.class));

        String exception =
                Objects.requireNonNull(
                                this.mockMvc
                                        .perform(
                                                post("/sendMsg")
                                                        .content(
                                                                this.objectMapper
                                                                        .writeValueAsString(
                                                                                MockObjectCreator
                                                                                        .getOrder()))
                                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isInternalServerError())
                                        .andReturn()
                                        .getResolvedException())
                        .getMessage();

        assertThat(exception)
                .isEqualTo(
                        "500 INTERNAL_SERVER_ERROR \"Unable To Parse Order"
                                + "(orderNumber=null, productId=null, amount=null)\"; nested exception "
                                + "is com.poc.boot.rabbitmq.controller.MessageControllerTest$1: Exception");
    }
}
