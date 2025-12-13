package com.example.highrps;

import com.example.highrps.repository.EventDto;
import com.example.highrps.repository.StatsEntity;
import com.example.highrps.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ScheduledBatchProcessor {

    private final RedisTemplate<String, String> redis;
    private final StatsRepository repo;
    private final JsonMapper mapper;

    private final String queueKey;
    private final int batchSize;

    public ScheduledBatchProcessor(RedisTemplate<String, String> redis, StatsRepository repo, JsonMapper mapper,
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
        List<String> items = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            String v = redis.opsForList().rightPop(queueKey);
            if (v == null) break;
            items.add(v);
        }
        if (items.isEmpty()) return;

        var entities = items.stream().map(s -> {
            try { return mapper.readValue(s, EventDto.class); } catch (Exception e) { return null; }
        }).filter(Objects::nonNull).map(d -> new StatsEntity(d.getId(), d.getValue())).toList();
        repo.saveAll(entities);
    }
}
