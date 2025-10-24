package com.poc.boot.rabbitmq.config;

import com.poc.boot.rabbitmq.entities.TrackingState;
import com.poc.boot.rabbitmq.repository.TrackingStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class RabbitMQConfig {

    public static final String DLX_ORDERS_EXCHANGE = "DLX.ORDERS.EXCHANGE";
    public static final String DLQ_ORDERS_QUEUE = "DLQ.ORDERS.QUEUE";

    public static final String ORDERS_QUEUE = "ORDERS.QUEUE";
    private static final String ORDERS_EXCHANGE = "ORDERS.EXCHANGE";
    private static final String ROUTING_KEY_ORDERS_QUEUE = "ROUTING_KEY_ORDERS_QUEUE";

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    private final TrackingStateRepository trackingStateRepository;

    public RabbitMQConfig(TrackingStateRepository trackingStateRepository) {
        this.trackingStateRepository = trackingStateRepository;
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
    RabbitTemplateCustomizer rabbitTemplateCustomizer() {
        return rabbitTemplate -> {
            rabbitTemplate.setConfirmCallback(
                    (correlationData, ack, cause) -> {
                        Assert.notNull(correlationData, () -> "correlationData can't be null");
                        log.info(
                                "correlation id : {} , acknowledgement : {}, cause : {}",
                                correlationData.getId(),
                                ack,
                                cause);
                        log.debug(
                                "persisted correlationId in db : {}",
                                trackingStateRepository.save(
                                        new TrackingState()
                                                .setCorrelationId(correlationData.getId())
                                                .setAck(ack)
                                                .setCause(cause)
                                                .setStatus("processed")));
                    });
            // This block ensures that returned, un-routable messages are logged.
            rabbitTemplate.setReturnsCallback(
                    returnedMessage ->
                            log.info(
                                    "Returned: {}\nreplyCode: {}\nreplyText: {}\nexchange/rk: {}/{}",
                                    returnedMessage.getMessage().toString(),
                                    returnedMessage.getReplyCode(),
                                    returnedMessage.getReplyText(),
                                    returnedMessage.getExchange(),
                                    returnedMessage.getRoutingKey()));
        };
    }

    @Bean
    JacksonJsonMessageConverter consumerJacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory(
            JacksonJsonMessageConverter consumerJacksonMessageConverter) {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory =
                new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(consumerJacksonMessageConverter);
        return messageHandlerMethodFactory;
    }
}
