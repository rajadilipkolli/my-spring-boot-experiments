package com.example.highrps.config;

import com.example.highrps.repository.EventDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    KafkaTemplate<String, EventDto> kafkaTemplate( ProducerFactory<String, EventDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    ConsumerFactory<String, EventDto> consumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "writer");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        cfg.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(cfg, new StringDeserializer(), new JacksonJsonDeserializer<>(EventDto.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, EventDto> kafkaListenerContainerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, EventDto>();
        f.setConsumerFactory(consumerFactory());
        return f;
    }

    @Bean
    ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregates-redis-writer");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(cfg, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, String>();
        f.setConsumerFactory(stringConsumerFactory());
        return f;
    }

    //kafka-streams needs topics to be created beforehand
    @Bean
    public KafkaAdmin.NewTopics eventsTopic() {
        return new KafkaAdmin.NewTopics(
                new NewTopic("events", 1, (short) 1),
                new NewTopic("stats-aggregates", 1, (short) 1),
                new NewTopic("stats-store", 1, (short) 1)
        );
    }
}
