package com.poc.boot.rabbitmq.config;

import com.poc.boot.rabbitmq.entities.TrackingState;
import com.poc.boot.rabbitmq.repository.TrackingStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class RabbitTemplateConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private static final Logger log = LoggerFactory.getLogger(RabbitTemplateConfirmCallback.class);
    private final TrackingStateRepository trackingStateRepository;

    public RabbitTemplateConfirmCallback(TrackingStateRepository trackingStateRepository) {
        this.trackingStateRepository = trackingStateRepository;
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        Assert.notNull(correlationData, () -> "correlationData can't be null");
        log.info(
                "correlation id : {} , acknowledgement : {}, cause : {}",
                correlationData.getId(),
                ack,
                cause);
        log.debug(
                "persisted correlationId in db : {}",
                this.trackingStateRepository.save(
                        new TrackingState()
                                .setCorrelationId(correlationData.getId())
                                .setStatus("processed")));
    }
}
