package com.example.highrps.batchprocessor;

import com.example.highrps.mapper.NewPostRequestToPostEntityMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.repository.PostRepository;
import com.example.highrps.repository.TagRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PostBatchProcessor implements EntityBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(PostBatchProcessor.class);

    private final NewPostRequestToPostEntityMapper mapper;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final JsonMapper jsonMapper;
    private final RedisTemplate<String, String> redis;

    public PostBatchProcessor(
            NewPostRequestToPostEntityMapper mapper,
            PostRepository postRepository,
            TagRepository tagRepository,
            JsonMapper jsonMapper,
            RedisTemplate<String, String> redis) {
        this.mapper = mapper;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.jsonMapper = jsonMapper;
        this.redis = redis;
    }

    @Override
    public String getEntityType() {
        return "post";
    }

    @Override
    public void processUpserts(List<String> payloads) {
        var entities = payloads.stream()
                .map(payload -> {
                    // Determine key first
                    String title = extractKey(payload);

                    // Skip if a recent tombstone exists for this title (prevents re-insert races).
                    // IMPORTANT: this Redis lookup is intentionally outside the JSON mapping try/catch so
                    // Redis failures don't get swallowed as "mapping errors".
                    if (title != null) {
                        Boolean deleted = redis.hasKey("deleted:post:" + title);
                        if (Boolean.TRUE.equals(deleted)) {
                            log.debug("Skipping upsert for title {} because recent tombstone present", title);
                            return null;
                        }
                    }

                    try {
                        NewPostRequest req = jsonMapper.readValue(payload, NewPostRequest.class);
                        return mapper.convert(req, tagRepository);
                    } catch (Exception e) {
                        log.warn("Failed to map post payload to entity: {}", payload, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            try {
                postRepository.saveAll(entities);
                log.debug("Persisted batch of {} post entities", entities.size());
            } catch (Exception e) {
                log.error("Failed to persist batch of {} post entities", entities.size(), e);
                throw e;
            }
        }
    }

    @Override
    public void processDeletes(List<String> keys) {
        if (keys.isEmpty()) return;
        try {
            int deleted = postRepository.deleteAllByTitleIn(keys);
            log.debug("Deleted {} post entities for {} keys", deleted, keys.size());
        } catch (Exception e) {
            log.warn("Failed to batch delete post entities for keys: {}", keys, e);
        }
    }

    @Override
    public String extractKey(String payload) {
        try {
            var node = jsonMapper.readTree(payload);
            return node.get("title").asString();
        } catch (Exception e) {
            log.warn("Failed to extract title from post payload", e);
            return null;
        }
    }
}
