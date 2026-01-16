package com.example.highrps.config;

import com.example.highrps.model.request.NewPostRequest;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration(proxyBeanMethods = false)
public class KafkaConfig {

    @Bean
    KafkaTemplate<String, NewPostRequest> kafkaTemplate(ProducerFactory<String, NewPostRequest> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    ConsumerFactory<String, NewPostRequest> consumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "writer");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        cfg.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.example.highrps.model.request");
        return new DefaultKafkaConsumerFactory<>(
                cfg, new StringDeserializer(), new JacksonJsonDeserializer<>(NewPostRequest.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, NewPostRequest> kafkaListenerContainerFactory(
            ConsumerFactory<String, NewPostRequest> consumerFactory) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, NewPostRequest>();
        f.setConsumerFactory(consumerFactory);
        return f;
    }

    @Bean
    ConsumerFactory<String, String> stringConsumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregates-redis-writer");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(cfg, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    ConsumerFactory<String, NewPostRequest> newPostConsumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregates-redis-writer");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        cfg.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.example.highrps.model.request");
        return new DefaultKafkaConsumerFactory<>(
                cfg, new StringDeserializer(), new JacksonJsonDeserializer<>(NewPostRequest.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, NewPostRequest> newPostKafkaListenerContainerFactory(
            ConsumerFactory<String, NewPostRequest> newPostConsumerFactory) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, NewPostRequest>();
        f.setConsumerFactory(newPostConsumerFactory);
        return f;
    }

    // Application-level topics. Kafka Streams will create internal changelog topics automatically.
    @Bean
    KafkaAdmin.NewTopics eventsTopic(
            @Value("${app.kafka.events-topic.partitions:3}") int eventsPartitions,
            @Value("${app.kafka.events-topic.replication-factor:1}") short eventsReplication,
            @Value("${app.kafka.posts-aggregates-topic.partitions:3}") int postsAggregatesPartitions,
            @Value("${app.kafka.posts-aggregates-topic.replication-factor:1}") short postsAggregatesReplication) {
        return new KafkaAdmin.NewTopics(
                new NewTopic("events", eventsPartitions, eventsReplication),
                new NewTopic("posts-aggregates", postsAggregatesPartitions, postsAggregatesReplication));
    }
}
