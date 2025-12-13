package com.example.highrps.service;

import com.example.highrps.StatsResponse;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class HelloService {

    private final RedisTemplate<String, String> redis;
    private final Cache<String, String> localCache;
    private final StreamsBuilderFactoryBean kafkaStreamsFactory;
    private ReadOnlyKeyValueStore<String, Long> keyValueStore = null;

    public HelloService(RedisTemplate<String, String> redis, Cache<String, String> localCache, StreamsBuilderFactoryBean kafkaStreamsFactory) {
        this.redis = redis;
        this.localCache = localCache;
        this.kafkaStreamsFactory = kafkaStreamsFactory;
    }

    public String ping() {
        return "pong";
    }

    public StatsResponse getStats(String id) {
        // 1) Local cache
        var cached = localCache.getIfPresent(id);
        if (cached != null) {
            return StatsResponse.fromJson(cached);
        }

        // 2) Redis materialized view
        var raw = redis.opsForValue().get("stats:" + id);
        if (raw != null) {
            localCache.put(id, raw);
            return StatsResponse.fromJson(raw);
        }

        // 3) Fallback: interactive query against Kafka Streams `stats-store` (materialized KTable)
        try {
            Long val = getKeyValueStore().get(id);
            if (val != null) {
                var resp = new StatsResponse(id, val);
                var json = StatsResponse.toJson(resp);
                localCache.put(id, json);
                return resp;
            }
        } catch (Exception e) {
            // ignore and fall through to empty
        }

        return StatsResponse.empty(id);
    }

    private ReadOnlyKeyValueStore<String, Long> getKeyValueStore() {
        if (keyValueStore == null) {
            KafkaStreams kafkaStreams = kafkaStreamsFactory.getKafkaStreams();
            Assert.notNull(kafkaStreams, () -> "Kafka Streams not initialized yet");
            this.keyValueStore = kafkaStreams
                    .store(StoreQueryParameters.fromNameAndType("stats-store", QueryableStoreTypes.keyValueStore()));
        }
        return keyValueStore;
    }
}

