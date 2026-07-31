package com.example.highrps.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaConfig {

    // Consumer factory for raw bytes (used by listeners that handle manual deserialization)
    @Bean
    ConsumerFactory<String, byte[]> newPostConsumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "new-posts-redis-writer");
        return getStringConsumerFactory(cfg);
    }

    @NonNull
    private ConsumerFactory<String, byte[]> getStringConsumerFactory(Map<String, Object> cfg) {
        cfg.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        cfg.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, byte[]> newPostKafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> newPostConsumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {
        return getStringConcurrentKafkaListenerContainerFactory(newPostConsumerFactory, kafkaTemplate);
    }

    @NonNull
    private ConcurrentKafkaListenerContainerFactory<String, byte[]> getStringConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> newPostConsumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
        f.setConsumerFactory(newPostConsumerFactory);
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        f.getContainerProperties().setStopImmediate(false);
        f.setConcurrency(32);
        f.setCommonErrorHandler(
                new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 2L)));
        return f;
    }

    // Consumer factory for Author bytes
    @Bean
    ConsumerFactory<String, byte[]> authorConsumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "authors-redis-writer");
        return getStringConsumerFactory(cfg);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, byte[]> authorKafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> authorConsumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {
        return getStringConcurrentKafkaListenerContainerFactory(authorConsumerFactory, kafkaTemplate);
    }

    // Consumer factory for PostComment bytes
    @Bean
    ConsumerFactory<String, byte[]> postCommentConsumerFactory(KafkaConnectionDetails kafkaConnectionDetails) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, "post-comments-redis-writer");
        return getStringConsumerFactory(cfg);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, byte[]> postCommentKafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> postCommentConsumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {
        return getStringConcurrentKafkaListenerContainerFactory(postCommentConsumerFactory, kafkaTemplate);
    }

    // Application-level topics. Kafka Streams will create internal changelog topics
    // automatically.
    @Bean
    KafkaAdmin.NewTopics eventsTopic(
            @Value("${app.kafka.events-topic.partitions:3}") int eventsPartitions,
            @Value("${app.kafka.events-topic.replication-factor:1}") short eventsReplication,
            @Value("${app.kafka.posts-aggregates-topic.partitions:3}") int postsAggregatesPartitions,
            @Value("${app.kafka.posts-aggregates-topic.replication-factor:1}") short postsAggregatesReplication,
            @Value("${app.kafka.authors-aggregates-topic.partitions:3}") int authorsAggregatesPartitions,
            @Value("${app.kafka.authors-aggregates-topic.replication-factor:1}") short authorsAggregatesReplication,
            @Value("${app.kafka.post-comments-aggregates-topic.partitions:3}") int postCommentsAggregatesPartitions,
            @Value("${app.kafka.post-comments-aggregates-topic.replication-factor:1}")
                    short postCommentsAggregatesReplication,
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
        NewTopic postComments = new NewTopic(
                "post-comments-aggregates", postCommentsAggregatesPartitions, postCommentsAggregatesReplication);
        postComments.configs(Map.of("cleanup.policy", "compact"));
        return new KafkaAdmin.NewTopics(events, posts, authors, postComments);
    }
}
