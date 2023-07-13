package com.poc.boot.rabbitmq.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.boot.rabbitmq.config.RabbitMQConfig;
import com.poc.boot.rabbitmq.util.MockObjectCreator;
import java.io.Serial;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class OrderMessageSenderTest {

    @Mock private RabbitTemplate rabbitTemplate;

    @Mock private ObjectMapper objectMapper;

    @InjectMocks private OrderMessageSenderImpl orderMessageSender;

    @Test
    void testSendOrder() throws JsonProcessingException {
        String convertedString =
                "{\"orderNumber\":\"1\",\"productId\":\"P1\"," + "\"amount\":10.0}";
        // given
        given(this.objectMapper.writeValueAsString(MockObjectCreator.getOrder()))
                .willReturn(convertedString);
        willDoNothing()
                .given(this.rabbitTemplate)
                .convertAndSend(
                        eq(RabbitMQConfig.QUEUE_ORDERS),
                        eq(this.orderMessageSender.getRabbitMQMessage(convertedString)),
                        any(CorrelationData.class));
        // when
        this.orderMessageSender.sendOrder(MockObjectCreator.getOrder());
        verify(this.rabbitTemplate, times(1))
                .convertAndSend(
                        eq(RabbitMQConfig.QUEUE_ORDERS),
                        eq(this.orderMessageSender.getRabbitMQMessage(convertedString)),
                        any(CorrelationData.class));
        verify(this.objectMapper, times(1)).writeValueAsString(MockObjectCreator.getOrder());
    }

    @Test
    void testSendOrderException() throws Exception {
        given(this.objectMapper.writeValueAsString(MockObjectCreator.getOrder()))
                .willThrow(
                        new JsonProcessingException("Exception") {
                            @Serial private static final long serialVersionUID = 1L;
                        });
        // when
        assertThatThrownBy(() -> this.orderMessageSender.sendOrder(MockObjectCreator.getOrder()))
                .hasMessage("Exception");
        verify(this.objectMapper, times(1)).writeValueAsString(MockObjectCreator.getOrder());
    }
}
