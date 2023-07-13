package com.poc.boot.rabbitmq.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.boot.rabbitmq.config.RabbitMQConfig;
import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.service.OrderMessageSender;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderMessageSenderImpl implements OrderMessageSender {

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void sendOrder(Order order) throws JsonProcessingException {
        // this.rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_ORDERS, order);

        String orderJson = this.objectMapper.writeValueAsString(order);
        String correlationId = UUID.randomUUID().toString();
        this.rabbitTemplate.convertAndSend(
                RabbitMQConfig.QUEUE_ORDERS,
                getRabbitMQMessage(orderJson),
                new CorrelationData(correlationId));
    }

    protected Message getRabbitMQMessage(String orderJson) {
        return MessageBuilder.withBody(orderJson.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
    }
}
