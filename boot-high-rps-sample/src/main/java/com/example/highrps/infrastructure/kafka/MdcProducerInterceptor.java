package com.example.highrps.infrastructure.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

public class MdcProducerInterceptor implements ProducerInterceptor<Object, Object> {

    public static final String CORRELATION_ID_HEADER = "correlationId";
    public static final String MDC_CORRELATION_ID_KEY = "correlationId";

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        String correlationId = MDC.get(MDC_CORRELATION_ID_KEY);
        if (correlationId != null) {
            record.headers().add(CORRELATION_ID_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // No-op
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // No-op
    }
}
