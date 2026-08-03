package com.example.ultimatepostgres.service;

import com.example.ultimatepostgres.model.CacheEntity;
import com.example.ultimatepostgres.repository.CacheRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
public class CacheService {

    private final CacheRepository cacheRepository;

    public CacheService(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Transactional
    public void put(String key, JsonNode value, long ttlMillis) {
        if (ttlMillis <= 0 || ttlMillis > 31536000000L) {
            throw new IllegalArgumentException("TTL must be positive and within 1 year");
        }
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(ttlMillis, java.time.temporal.ChronoUnit.MILLIS);
        cacheRepository.upsert(key, value.toString(), expiresAt);
    }

    @Transactional(readOnly = true)
    public Optional<JsonNode> get(String key) {
        return cacheRepository.findValidByKey(key, OffsetDateTime.now()).map(CacheEntity::getValue);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> getByPrefix(String prefix) {
        String escapedPrefix = prefix.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return cacheRepository.findValidByPrefix(escapedPrefix + "%", OffsetDateTime.now()).stream()
                .map(CacheEntity::getValue)
                .toList();
    }

    @Transactional
    public void evict(String key) {
        cacheRepository.deleteById(key);
    }

    @Transactional
    public void evictAll() {
        cacheRepository.truncate();
    }
}
