package com.example.highrps;

import com.example.highrps.repository.EventDto;
import com.example.highrps.repository.StatsEntity;
import com.example.highrps.repository.StatsRepository;
import java.util.List;
import java.util.Objects;
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
    private final StatsRepository repo;
    private final JsonMapper mapper;

    private final String queueKey;
    private final int batchSize;

    public ScheduledBatchProcessor(
            RedisTemplate<String, String> redis,
            StatsRepository repo,
            JsonMapper mapper,
            @Value("${app.batch.queue-key}") String queueKey,
            @Value("${app.batch.size}") int batchSize) {
        this.redis = redis;
        this.repo = repo;
        this.mapper = mapper;
        this.queueKey = queueKey;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.batch.delay-ms}")
    public void processBatch() {
        List<String> items = redis.opsForList().rightPop(queueKey, batchSize);
        if (items.isEmpty()) {
            return;
        }

        var entities = items.stream()
                .filter(Objects::nonNull)
                .map(s -> {
                    try {
                        return mapper.readValue(s, EventDto.class);
                    } catch (Exception e) {
                        log.warn("Failed to deserialize event: {}", s, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(d -> new StatsEntity(d.getId(), d.getValue()))
                .toList();
        if (!entities.isEmpty()) {
            try {
                repo.saveAll(entities);
            } catch (Exception e) {
                log.error("Failed to persist batch of {} entities, re-queuing", entities.size(), e);
                // Re-queue items to avoid data loss
                items.forEach(item -> redis.opsForList().leftPush(queueKey, item));
                throw e;
            }
        }
    }
}
