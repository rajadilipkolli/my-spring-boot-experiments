package com.example.highrps.post.listener;

import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.domain.PostRedis;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.shared.AbstractAggregatesToRedisListener;
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
public class PostAggregatesToRedisListener extends AbstractAggregatesToRedisListener<NewPostRequest> {

    private final PostRedisRepository postRedisRepository;

    public PostAggregatesToRedisListener(
            RedisTemplate<String, String> redis,
            @Value("${app.batch.queue-key:events:queue}") String queueKey,
            JsonMapper jsonMapper,
            PostRedisRepository postRedisRepository,
            DeletionMarkerHandler deletionMarkerHandler) {
        super(redis, queueKey, jsonMapper, deletionMarkerHandler, NewPostRequest.class);
        this.postRedisRepository = postRedisRepository;
    }

    @Override
    protected void deleteFromRepository(String key) {
        postRedisRepository.deleteById(Long.valueOf(key));
    }

    @Override
    protected void saveToRepository(NewPostRequest payload, String key) {
        PostRedis redisEntity = new PostRedis()
                .setId(payload.postId())
                .setTitle(payload.title())
                .setContent(payload.content())
                .setAuthorEmail(payload.email())
                .setPublished(payload.published())
                .setPublishedAt(payload.publishedAt())
                .setCreatedAt(payload.createdAt())
                .setModifiedAt(payload.modifiedAt())
                .setDetails(payload.details())
                .setTags(payload.tags());
        postRedisRepository.save(redisEntity);
    }

    @Override
    protected String getEntityType() {
        return "post";
    }

    @Override
    protected String getMarkerType() {
        return DeletionMarkerHandler.POST;
    }

    @Override
    protected String getDeletionIdentifierField() {
        return "postId";
    }

    @KafkaListener(
            topics = "posts-aggregates",
            groupId = "new-posts-redis-writer",
            containerFactory = "newPostKafkaListenerContainerFactory")
    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 500, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    public void handleAggregate(ConsumerRecord<String, byte[]> record) {
        processAggregate(record, "posts-aggregates");
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, byte[]> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        handleDlt(record, topic);
    }
}
