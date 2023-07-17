package com.poc.boot.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

@EnableRabbit
@Configuration
@Slf4j
public class RabbitMQConfig implements RabbitListenerConfigurer {

    public static final String DLX_ORDERS_EXCHANGE = "DLX.ORDERS.EXCHANGE";

    public static final String DLQ_ORDERS_QUEUE = "DLQ.ORDERS.QUEUE";

    public static final String ORDERS_QUEUE = "ORDERS.QUEUE";

    private static final String ORDERS_EXCHANGE = "ORDERS.EXCHANGE";

    private static final String ROUTING_KEY_ORDERS_QUEUE = "ROUTING_KEY_ORDERS_QUEUE";

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
    Binding bindingMessages() {
        return BindingBuilder.bind(ordersQueue())
                .to(ordersExchange())
                .with(ROUTING_KEY_ORDERS_QUEUE);
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
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    /* Bean for rabbitTemplate */
    @Bean
    RabbitTemplate templateWithConfirmsEnabled(
            final ConnectionFactory connectionFactory,
            final Jackson2JsonMessageConverter producerJackson2MessageConverter) {
        final RabbitTemplate templateWithConfirmsEnabled = new RabbitTemplate(connectionFactory);
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10_000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        templateWithConfirmsEnabled.setRetryTemplate(retryTemplate);
        templateWithConfirmsEnabled.setMessageConverter(producerJackson2MessageConverter);
        templateWithConfirmsEnabled.setConfirmCallback(
                (correlationData, acknowledgement, cause) -> {
                    Assert.notNull(correlationData, () -> "correlationData can't be null");
                    log.info(
                            "correlation id : {} , acknowledgement : {}, cause : {}",
                            correlationData.getId(),
                            acknowledgement,
                            cause);
                });
        return templateWithConfirmsEnabled;
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
    MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory =
                new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(consumerJackson2MessageConverter());
        return messageHandlerMethodFactory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }
}
