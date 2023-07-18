package com.poc.boot.rabbitmq.config;

import com.poc.boot.rabbitmq.entities.TrackingState;
import com.poc.boot.rabbitmq.repository.TrackingStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTemplateConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private final TrackingStateRepository trackingStateRepository;

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
                        new TrackingState(null, correlationData.getId(), "processed")));
    }
}
