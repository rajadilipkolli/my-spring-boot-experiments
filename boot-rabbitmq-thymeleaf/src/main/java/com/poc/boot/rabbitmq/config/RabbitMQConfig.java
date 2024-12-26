package com.poc.boot.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

@Configuration(proxyBeanMethods = false)
public class RabbitMQConfig {

    public static final String DLX_ORDERS_EXCHANGE = "DLX.ORDERS.EXCHANGE";

    public static final String DLQ_ORDERS_QUEUE = "DLQ.ORDERS.QUEUE";

    public static final String ORDERS_QUEUE = "ORDERS.QUEUE";

    private static final String ORDERS_EXCHANGE = "ORDERS.EXCHANGE";

    private static final String ROUTING_KEY_ORDERS_QUEUE = "ROUTING_KEY_ORDERS_QUEUE";

    private final RabbitTemplateConfirmCallback rabbitTemplateConfirmCallback;

    RabbitMQConfig(RabbitTemplateConfirmCallback rabbitTemplateConfirmCallback) {
        this.rabbitTemplateConfirmCallback = rabbitTemplateConfirmCallback;
    }

    @Bean
    Queue ordersQueue() {
        return QueueBuilder.durable(ORDERS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_ORDERS_EXCHANGE)
                .build();
    }

    @Bean
    DirectExchange ordersExchange() {
        return ExchangeBuilder.directExchange(ORDERS_EXCHANGE).build();
    }

    /* Binding between Exchange and Queue using routing key */
    @Bean
    Binding bindingMessages(DirectExchange ordersExchange, Queue ordersQueue) {
        return BindingBuilder.bind(ordersQueue).to(ordersExchange).with(ROUTING_KEY_ORDERS_QUEUE);
    }

    @Bean
    FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(DLX_ORDERS_EXCHANGE).build();
    }

    /**
     * We may want to send invalid messages to a separate queue so that we can inspect and reprocess
     * them later. We can use DLQ concept to automatically do it instead of we manually write the
     * code to handle such scenarios. Now try to send an invalid JSON message to orders-queue, it
     * will be sent to dead-orders-queue.
     *
     * @return rabbitMQ Queue.
     */
    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_ORDERS_QUEUE).build();
    }

    /* Binding between Exchange and Queue for Dead Letter */
    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    @Bean
    RabbitTemplateCustomizer rabbitTemplateCustomizer(
            Jackson2JsonMessageConverter producerJackson2MessageConverter) {
        return rabbitTemplate -> {
            rabbitTemplate.setMessageConverter(producerJackson2MessageConverter);
            rabbitTemplate.setConfirmCallback(rabbitTemplateConfirmCallback);
        };
    }

    @Bean
    Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory(
            MappingJackson2MessageConverter consumerJackson2MessageConverter) {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory =
                new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(consumerJackson2MessageConverter);
        return messageHandlerMethodFactory;
    }
}
