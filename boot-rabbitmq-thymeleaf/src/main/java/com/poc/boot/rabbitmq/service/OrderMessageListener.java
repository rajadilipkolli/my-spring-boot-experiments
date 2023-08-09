package com.poc.boot.rabbitmq.service;

import static com.poc.boot.rabbitmq.config.RabbitMQConfig.DLQ_ORDERS_QUEUE;
import static com.poc.boot.rabbitmq.config.RabbitMQConfig.ORDERS_QUEUE;

import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.repository.TrackingStateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class OrderMessageListener {

    private final TrackingStateRepository trackingStateRepository;

    public OrderMessageListener(TrackingStateRepository trackingStateRepository) {
        this.trackingStateRepository = trackingStateRepository;
    }

    @RabbitListener(queues = ORDERS_QUEUE)
    public void processOrder(Order order) {
        log.debug("Order Received: {}", order);
        if (order.amount() < 0) {
            throw new AmqpRejectAndDontRequeueException("Order Rejected");
        }
    }

    // Dead letter processing
    @RabbitListener(queues = DLQ_ORDERS_QUEUE)
    public void processFailedMessages(Message<?> message) {
        log.debug("Received failed message: {}", message.toString());
        String correlationId =
                (String) message.getHeaders().get("spring_returned_message_correlation");
        log.debug("correlationId : {} ", correlationId);
        int rows = trackingStateRepository.updateStatusByCorrelationId("failed", correlationId);
        log.debug("Updated rows : {}", rows);
    }
}
