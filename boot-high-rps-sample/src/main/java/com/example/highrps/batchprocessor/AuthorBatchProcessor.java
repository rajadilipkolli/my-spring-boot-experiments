package com.example.highrps.batchprocessor;

import com.example.highrps.mapper.AuthorRequestToEntityMapper;
import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.repository.jpa.AuthorRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuthorBatchProcessor implements EntityBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(AuthorBatchProcessor.class);

    private final AuthorRequestToEntityMapper mapper;
    private final AuthorRepository authorRepository;
    private final JsonMapper jsonMapper;
    private final RedisTemplate<String, String> redis;

    public AuthorBatchProcessor(
            AuthorRequestToEntityMapper mapper,
            AuthorRepository authorRepository,
            JsonMapper jsonMapper,
            RedisTemplate<String, String> redis) {
        this.mapper = mapper;
        this.authorRepository = authorRepository;
        this.jsonMapper = jsonMapper;
        this.redis = redis;
    }

    @Override
    public String getEntityType() {
        return "author";
    }

    @Override
    public void processUpserts(List<String> payloads) {
        var entities = payloads.stream()
                .map(payload -> {
                    // Determine key first
                    String email = extractKey(payload);

                    // Skip if a recent tombstone exists for this email (prevents re-insert races).
                    // IMPORTANT: this Redis lookup is intentionally outside the JSON mapping try/catch so
                    // Redis failures don't get swallowed as "mapping errors".
                    if (email != null) {
                        Boolean deleted = redis.hasKey("deleted:authors:" + email);
                        if (Boolean.TRUE.equals(deleted)) {
                            log.debug("Skipping upsert for email {} because recent tombstone present", email);
                            return null;
                        }
                    }
                    try {
                        AuthorRequest req = jsonMapper.readValue(payload, AuthorRequest.class);
                        return mapper.convert(req);
                    } catch (Exception e) {
                        log.warn("Failed to map author payload to entity: {}", payload, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            try {
                authorRepository.saveAll(entities);
                log.debug("Persisted batch of {} author entities", entities.size());
            } catch (Exception e) {
                log.error("Failed to persist batch of {} author entities", entities.size(), e);
                throw e;
            }
        }
    }

    @Override
    public void processDeletes(List<String> keys) {
        if (keys.isEmpty()) return;
        try {
            long deletedRows = authorRepository.deleteByEmailInAllIgnoreCase(keys);
            log.debug("Deleted {} author entities for {} keys", deletedRows, keys.size());
        } catch (Exception e) {
            log.warn("Failed to batch delete author entities for keys: {}", keys, e);
        }
    }

    @Override
    public String extractKey(String payload) {
        try {
            var node = jsonMapper.readTree(payload);
            String email = node.path("email").asString(null);
            if (email == null || email.isBlank()) {
                log.warn("Author payload missing email");
                return null;
            }
            return email.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            log.warn("Failed to extract email from author payload", e);
            return null;
        }
    }
}
