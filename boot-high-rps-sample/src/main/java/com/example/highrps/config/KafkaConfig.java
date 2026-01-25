package com.example.highrps.config;

import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.request.NewPostRequest;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@Configuration(proxyBeanMethods = false)
public class KafkaConfig {

    // Producer: generic Object-valued KafkaTemplate so no runtime casts are needed
    @Bean
    ProducerFactory<String, Object> producerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        // Any additional producer tuning can go here
        return new DefaultKafkaProducerFactory<>(cfg, new StringSerializer(), new JacksonJsonSerializer<>());
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // Keep a String-valued ConsumerFactory and factory for listeners that consume String payloads.
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
    ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory(
            ConsumerFactory<String, String> stringConsumerFactory) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, String>();
        f.setConsumerFactory(stringConsumerFactory);
        return f;
    }

    // Consumer factory for NewPostRequest (used by listeners that consume typed payloads)
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

    // Consumer factory for AuthorRequest (used by listeners that consume typed author payloads)
    @Bean
    ConsumerFactory<String, AuthorRequest> authorConsumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "authors-redis-writer");
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        cfg.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.example.highrps.model.request");
        return new DefaultKafkaConsumerFactory<>(
                cfg, new StringDeserializer(), new JacksonJsonDeserializer<>(AuthorRequest.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, AuthorRequest> authorKafkaListenerContainerFactory(
            ConsumerFactory<String, AuthorRequest> authorConsumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, AuthorRequest>();
        f.setConsumerFactory(authorConsumerFactory);

        return f;
    }

    // Application-level topics. Kafka Streams will create internal changelog topics automatically.
    @Bean
    KafkaAdmin.NewTopics eventsTopic(
            @Value("${app.kafka.events-topic.partitions:3}") int eventsPartitions,
            @Value("${app.kafka.events-topic.replication-factor:1}") short eventsReplication,
            @Value("${app.kafka.posts-aggregates-topic.partitions:3}") int postsAggregatesPartitions,
            @Value("${app.kafka.posts-aggregates-topic.replication-factor:1}") short postsAggregatesReplication,
            @Value("${app.kafka.authors-aggregates-topic.partitions:3}") int authorsAggregatesPartitions,
            @Value("${app.kafka.authors-aggregates-topic.replication-factor:1}") short authorsAggregatesReplication,
            @Value("${app.kafka.events-topic.tombstone-retention-ms:604800000}") long tombstoneRetentionMs) {

        NewTopic events = new NewTopic("events", eventsPartitions, eventsReplication);
        Map<String, String> eventsCfg = new HashMap<>();
        eventsCfg.put("cleanup.policy", "compact,delete");
        eventsCfg.put("delete.retention.ms", String.valueOf(tombstoneRetentionMs));
        events.configs(eventsCfg);

        NewTopic posts = new NewTopic("posts-aggregates", postsAggregatesPartitions, postsAggregatesReplication);
        posts.configs(Map.of("cleanup.policy", "compact"));
        NewTopic authors =
                new NewTopic("authors-aggregates", authorsAggregatesPartitions, authorsAggregatesReplication);
        authors.configs(Map.of("cleanup.policy", "compact"));
        return new KafkaAdmin.NewTopics(events, posts, authors);
    }
}
