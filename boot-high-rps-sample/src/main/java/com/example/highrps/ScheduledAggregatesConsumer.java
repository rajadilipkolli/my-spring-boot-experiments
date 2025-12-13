package com.example.highrps;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;
import java.util.Collections;

@Component
public class ScheduledAggregatesConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScheduledAggregatesConsumer.class);

    private final RedisTemplate<String,String> redis;
    private final KafkaConsumer<String, String> consumer;
    private final String queueKey;

    public ScheduledAggregatesConsumer(KafkaProperties kafkaProperties, RedisTemplate<String, String> redis, @Value("${app.batch.queue-key}") String queueKey) {
        this.redis = redis;
        this.queueKey = queueKey;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregates-writer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList("stats-aggregates"));
    }

    @Scheduled(fixedDelayString = "${app.batch.delay-ms:5000}")
    public void pollAndWrite() {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        if (records.isEmpty()) return;

        log.info("Polled {} aggregate records from Kafka", records.count());
        records.forEach(r -> {
            try {
                Long v = Long.valueOf(r.value());
                var json = StatsResponse.toJson(new StatsResponse(r.key(), v));
                redis.opsForValue().set("stats:" + r.key(), json);
                redis.opsForList().leftPush(queueKey, json);
            } catch (Exception ignore) {
            }
        });
        consumer.commitSync();
    }
}
