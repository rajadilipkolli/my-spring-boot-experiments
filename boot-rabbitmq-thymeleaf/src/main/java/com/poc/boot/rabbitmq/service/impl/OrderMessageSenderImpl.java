package com.poc.boot.rabbitmq.service.impl;

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
import tools.jackson.databind.json.JsonMapper;

@Service
public class OrderMessageSenderImpl implements OrderMessageSender {

    private final RabbitTemplate rabbitTemplate;

    private final JsonMapper jsonMapper;

    public OrderMessageSenderImpl(RabbitTemplate rabbitTemplate, JsonMapper jsonMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void sendOrder(Order order) {
        // this.rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_ORDERS, order);

        String orderJson = this.jsonMapper.writeValueAsString(order);
        String correlationId = UUID.randomUUID().toString();
        this.rabbitTemplate.convertAndSend(
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
