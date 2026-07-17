package com.example.highrps.author.listener;

import com.example.highrps.author.domain.AuthorRedis;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.dto.AuthorRequest;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.shared.AbstractAggregatesToRedisListener;
import java.util.Locale;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuthorAggregatesToRedisListener extends AbstractAggregatesToRedisListener<AuthorRequest> {

    private final AuthorRedisRepository authorRedisRepository;

    public AuthorAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            AuthorRedisRepository authorRedisRepository,
            DeletionMarkerHandler deletionMarkerHandler) {
        super(redis, queueKey, jsonMapper, deletionMarkerHandler, AuthorRequest.class);
        this.authorRedisRepository = authorRedisRepository;
    }

    @Override
    protected void deleteFromRepository(String key) {
        authorRedisRepository.deleteById(key);
    }

    @Override
    protected void saveToRepository(AuthorRequest payload, String key) {
        AuthorRedis redisEntity = new AuthorRedis()
                .setEmail(payload.email() == null ? null : payload.email().toLowerCase(Locale.ROOT))
                .setFirstName(payload.firstName())
                .setMiddleName(payload.middleName())
                .setLastName(payload.lastName())
                .setMobile(payload.mobile())
                .setRegisteredAt(payload.registeredAt());
        redisEntity.setCreatedAt(payload.createdAt());
        redisEntity.setModifiedAt(payload.modifiedAt());
        authorRedisRepository.save(redisEntity);
    }

    @Override
    protected String getEntityType() {
        return "author";
    }

    @Override
    protected String getMarkerType() {
        return DeletionMarkerHandler.AUTHOR;
    }

    @Override
    protected String getDeletionIdentifierField() {
        return "email";
    }

    @KafkaListener(
            topics = "authors-aggregates",
            groupId = "authors-redis-writer",
            containerFactory = "authorKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, byte[]> record) {
        processAggregate(record, "authors-aggregates");
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, byte[]> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        handleDlt(record, topic);
    }
}
