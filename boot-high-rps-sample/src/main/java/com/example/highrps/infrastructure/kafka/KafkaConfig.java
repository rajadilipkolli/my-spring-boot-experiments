package com.example.highrps.infrastructure.kafka;

import com.example.highrps.shared.config.AppProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
@EnableKafkaStreams
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
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        cfg.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        cfg.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ByteArrayDeserializer.class);
        cfg.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    ConsumerRecordRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        Map<String, Object> stringProps =
                new HashMap<>(kafkaTemplate.getProducerFactory().getConfigurationProperties());
        stringProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        stringProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        DeadLetterPublishingRecoverer stringRecoverer =
                new DeadLetterPublishingRecoverer(new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(stringProps)));

        Map<String, Object> byteProps =
                new HashMap<>(kafkaTemplate.getProducerFactory().getConfigurationProperties());
        byteProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        byteProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        DeadLetterPublishingRecoverer byteRecoverer =
                new DeadLetterPublishingRecoverer(new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(byteProps)));

        return (record, exception) -> {
            if (record.key() instanceof byte[]) {
                byteRecoverer.accept(record, exception);
            } else {
                stringRecoverer.accept(record, exception);
            }
        };
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, byte[]> newPostKafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> newPostConsumerFactory, ConsumerRecordRecoverer recoverer) {
        return getStringConcurrentKafkaListenerContainerFactory(newPostConsumerFactory, recoverer);
    }

    @NonNull
    private ConcurrentKafkaListenerContainerFactory<String, byte[]> getStringConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> consumerFactory, ConsumerRecordRecoverer recoverer) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
        f.setConsumerFactory(consumerFactory);
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        f.getContainerProperties().setStopImmediate(false);
        f.setConcurrency(32);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
        errorHandler.setCommitRecovered(true);
        f.setCommonErrorHandler(errorHandler);
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
            ConsumerFactory<String, byte[]> authorConsumerFactory, ConsumerRecordRecoverer recoverer) {
        return getStringConcurrentKafkaListenerContainerFactory(authorConsumerFactory, recoverer);
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
            ConsumerFactory<String, byte[]> postCommentConsumerFactory, ConsumerRecordRecoverer recoverer) {
        return getStringConcurrentKafkaListenerContainerFactory(postCommentConsumerFactory, recoverer);
    }

    // Application-level topics. Kafka Streams will create internal changelog topics
    // automatically.
    @Bean
    KafkaAdmin.NewTopics eventsTopic(AppProperties appProperties) {
        int eventsPartitions = appProperties.getKafka().getEventsTopic().getPartitions();
        short eventsReplication = appProperties.getKafka().getEventsTopic().getReplicationFactor();
        int postsAggregatesPartitions =
                appProperties.getKafka().getPostsAggregatesTopic().getPartitions();
        short postsAggregatesReplication =
                appProperties.getKafka().getPostsAggregatesTopic().getReplicationFactor();
        int authorsAggregatesPartitions =
                appProperties.getKafka().getAuthorsAggregatesTopic().getPartitions();
        short authorsAggregatesReplication =
                appProperties.getKafka().getAuthorsAggregatesTopic().getReplicationFactor();
        int postCommentsAggregatesPartitions =
                appProperties.getKafka().getPostCommentsAggregatesTopic().getPartitions();
        short postCommentsAggregatesReplication =
                appProperties.getKafka().getPostCommentsAggregatesTopic().getReplicationFactor();
        long tombstoneRetentionMs = appProperties.getKafka().getEventsTopic().getTombstoneRetentionMs();
        String minInSyncReplicas = appProperties.getKafka().getMinInsyncReplicas();

        NewTopic events = new NewTopic("events", eventsPartitions, eventsReplication);
        Map<String, String> eventsCfg = new HashMap<>();
        eventsCfg.put("cleanup.policy", "compact,delete");
        eventsCfg.put("delete.retention.ms", String.valueOf(tombstoneRetentionMs));
        eventsCfg.put("min.insync.replicas", minInSyncReplicas);
        events.configs(eventsCfg);

        NewTopic posts = new NewTopic("posts-aggregates", postsAggregatesPartitions, postsAggregatesReplication);
        posts.configs(Map.of("cleanup.policy", "compact", "min.insync.replicas", minInSyncReplicas));
        NewTopic authors =
                new NewTopic("authors-aggregates", authorsAggregatesPartitions, authorsAggregatesReplication);
        authors.configs(Map.of("cleanup.policy", "compact", "min.insync.replicas", minInSyncReplicas));
        NewTopic postComments = new NewTopic(
                "post-comments-aggregates", postCommentsAggregatesPartitions, postCommentsAggregatesReplication);
        postComments.configs(Map.of("cleanup.policy", "compact", "min.insync.replicas", minInSyncReplicas));
        return new KafkaAdmin.NewTopics(events, posts, authors, postComments);
    }
}
