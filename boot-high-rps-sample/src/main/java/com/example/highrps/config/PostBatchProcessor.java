package com.example.highrps.config;

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
                    try {
                        // Skip if a recent tombstone exists for this title (prevents re-insert races)
                        String title = extractKey(payload);
                        if (title != null) {
                            Boolean deleted = redis.opsForSet().isMember("deleted:posts", title);
                            if (Boolean.TRUE.equals(deleted)) {
                                log.debug("Skipping upsert for title {} because recent tombstone present", title);
                                return null;
                            }
                        }
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
        keys.forEach(title -> {
            try {
                if (postRepository.existsByTitle(title)) {
                    postRepository.deleteByTitle(title);
                    log.debug("Deleted post entity for title: {}", title);
                }
            } catch (Exception e) {
                log.warn("Failed to delete post entity for title: {}", title, e);
            }
        });
    }

    @Override
    public String extractKey(String payload) {
        try {
            var node = jsonMapper.readTree(payload);
            return node.get("title").asText();
        } catch (Exception e) {
            log.warn("Failed to extract title from post payload", e);
            return null;
        }
    }
}
