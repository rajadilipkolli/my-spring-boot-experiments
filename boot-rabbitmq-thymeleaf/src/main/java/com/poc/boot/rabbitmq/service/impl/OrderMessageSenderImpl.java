package com.poc.boot.rabbitmq.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.boot.rabbitmq.config.RabbitMQConfig;
import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.service.OrderMessageSender;
import java.util.UUID;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderMessageSenderImpl implements OrderMessageSender {

    private final RabbitTemplate templateWithConfirmsEnabled;

    private final ObjectMapper objectMapper;

    public OrderMessageSenderImpl(
            RabbitTemplate templateWithConfirmsEnabled, ObjectMapper objectMapper) {
        this.templateWithConfirmsEnabled = templateWithConfirmsEnabled;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendOrder(Order order) throws JsonProcessingException {
        // this.rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_ORDERS, order);

        String orderJson = this.objectMapper.writeValueAsString(order);
        String correlationId = UUID.randomUUID().toString();
        this.templateWithConfirmsEnabled.convertAndSend(
                RabbitMQConfig.ORDERS_QUEUE,
                getRabbitMQMessage(orderJson),
                new CorrelationData(correlationId));
    }

    protected Message getRabbitMQMessage(String orderJson) {
        return MessageBuilder.withBody(orderJson.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
    }
}
