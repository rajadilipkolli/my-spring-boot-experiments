package com.example.highrps.config;

import com.example.highrps.mapper.NewPostRequestToPostEntityMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.repository.PostRepository;
import com.example.highrps.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ScheduledBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledBatchProcessor.class);

    private final RedisTemplate<String, String> redis;
    private final NewPostRequestToPostEntityMapper newPostRequestToPostEntityMapper;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final JsonMapper jsonMapper;

    private final String queueKey;
    private final int batchSize;

    public ScheduledBatchProcessor(
            RedisTemplate<String, String> redis,
            NewPostRequestToPostEntityMapper newPostRequestToPostEntityMapper,
            PostRepository postRepository,
            TagRepository tagRepository,
            JsonMapper jsonMapper,
            @Value("${app.batch.queue-key}") String queueKey,
            @Value("${app.batch.size}") int batchSize) {
        this.redis = redis;
        this.newPostRequestToPostEntityMapper = newPostRequestToPostEntityMapper;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.jsonMapper = jsonMapper;
        this.queueKey = queueKey;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.batch.delay-ms}")
    public void processBatch() {
        List<String> items = redis.opsForList().rightPop(queueKey, batchSize);
        if (items == null || items.isEmpty()) {
            return;
        }

        // Deduplicate by title, keeping the latest value in the batch
        Map<String, Object> latestByTitle = items.stream()
                .filter(Objects::nonNull)
                .map(s -> {
                    try {
                        // Attempt to parse tombstone marker first
                        var node = jsonMapper.readTree(s);
                        if (node.has("__deleted") && node.get("__deleted").asBoolean(false)) {
                            // Represent tombstone with the raw title string
                            return Map.<String, Object>of(node.get("title").asString(), Boolean.TRUE);
                        }
                        // Otherwise, parse as NewPostRequest
                        NewPostRequest req = jsonMapper.readValue(s, NewPostRequest.class);
                        return Map.<String, Object>of(req.title(), req);
                    } catch (Exception e) {
                        log.warn("Failed to deserialize queued payload: {}", s, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (older, newer) -> newer));

        // Now separate deletes and upserts: if value is Boolean.TRUE => delete
        var deletes = latestByTitle.entrySet().stream()
                .filter(e -> Boolean.TRUE.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        var upserts = latestByTitle.values().stream()
                .filter(v -> v instanceof NewPostRequest)
                .map(v -> (NewPostRequest) v)
                .map(newPostRequest -> {
                    try {
                        return newPostRequestToPostEntityMapper.convert(newPostRequest, tagRepository);
                    } catch (Exception e) {
                        log.warn("Failed to map queued response to entity for title: {}", newPostRequest.title(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!upserts.isEmpty()) {
            try {
                postRepository.saveAll(upserts);
            } catch (Exception e) {
                log.error("Failed to persist batch of {} entities, re-queuing", upserts.size(), e);
                // Re-queue items to avoid data loss
                items.forEach(item -> redis.opsForList().leftPush(queueKey, item));
                throw e;
            }
        }

        if (!deletes.isEmpty()) {
            try {
                deletes.forEach(title -> {
                    try {
                        if (postRepository.existsByTitle(title)) {
                            postRepository.deleteByTitle(title);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to delete DB entity for title: {}", title, e);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to delete batch of {} entities, re-queuing", deletes.size(), e);
                items.forEach(item -> redis.opsForList().leftPush(queueKey, item));
                throw e;
            }
        }
    }
}
