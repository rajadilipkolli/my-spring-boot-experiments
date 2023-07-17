package com.poc.boot.rabbitmq.config;

import static com.poc.boot.rabbitmq.config.RabbitMQConfig.DLQ_ORDERS_QUEUE;
import static com.poc.boot.rabbitmq.config.RabbitMQConfig.ORDERS_QUEUE;

import com.poc.boot.rabbitmq.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class OrderMessageListener {

    @RabbitListener(queues = ORDERS_QUEUE)
    public void processOrder(Order order) {
        log.debug("Order Received: {}", order);
        if (order.getAmount() < 0) {
            throw new AmqpRejectAndDontRequeueException("Order Rejected");
        }
    }

    // Deadletter processing
    @RabbitListener(queues = DLQ_ORDERS_QUEUE)
    public void processFailedMessages(Message<?> message) {
        log.debug("Received failed message: {}", message.toString());
    }
}
