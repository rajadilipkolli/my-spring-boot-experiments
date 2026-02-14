package com.example.highrps.batchprocessor;

import com.example.highrps.entities.AuthorEntity;
import com.example.highrps.entities.PostEntity;
import com.example.highrps.mapper.NewPostRequestToPostEntityMapper;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.repository.jpa.AuthorRepository;
import com.example.highrps.repository.jpa.PostRepository;
import com.example.highrps.repository.jpa.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PostBatchProcessor implements EntityBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(PostBatchProcessor.class);

    private final NewPostRequestToPostEntityMapper mapper;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final JsonMapper jsonMapper;
    private final RedisTemplate<String, String> redis;
    private final AuthorRepository authorRepository;

    public PostBatchProcessor(
            NewPostRequestToPostEntityMapper mapper,
            PostRepository postRepository,
            TagRepository tagRepository,
            JsonMapper jsonMapper,
            RedisTemplate<String, String> redis,
            AuthorRepository authorRepository) {
        this.mapper = mapper;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.jsonMapper = jsonMapper;
        this.redis = redis;
        this.authorRepository = authorRepository;
    }

    @Override
    public String getEntityType() {
        return "post";
    }

    @Override
    @Transactional
    public void processUpserts(List<String> payloads) {
        // Step 1: Parse payloads and extract postIds, while filtering out any with recent tombstones
        List<ParsedPost> parsedPosts = payloads.stream()
                .map(payload -> {
                    String postId = extractKey(payload);
                    if (postId != null) {
                        // Skip if tombstone exists
                        Boolean deleted = redis.hasKey("deleted:post:" + postId);
                        if (Boolean.TRUE.equals(deleted)) {
                            log.debug("Skipping upsert for postId {} because recent tombstone present", postId);
                            return null;
                        }
                    }
                    try {
                        NewPostRequest req = jsonMapper.readValue(payload, NewPostRequest.class);
                        return new ParsedPost(postId, req);
                    } catch (Exception e) {
                        log.warn("Failed to map post payload to entity: {}", payload, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(p -> p.postId() != null)
                .toList();

        if (parsedPosts.isEmpty()) {
            return;
        }

        // Step 2: Extract all postIds and fetch existing posts from DB
        List<Long> postIds = parsedPosts.stream()
                .map(parsedPost -> Long.valueOf(parsedPost.postId))
                .toList();

        List<PostEntity> existingPosts = postRepository.findByPostRefIdIn(postIds);

        Map<Long, PostEntity> existingByPostId = existingPosts.stream()
                .collect(Collectors.toMap(PostEntity::getPostRefId, Function.identity(), (e1, e2) -> e1));

        // Step 3: Process each post - update existing or create new
        List<PostEntity> entitiesToSave = parsedPosts.stream()
                .map(parsed -> {
                    PostEntity entity = existingByPostId.get(Long.valueOf(parsed.postId()));
                    if (entity != null) {
                        // Update existing entity
                        try {
                            mapper.updatePostEntity(parsed.request(), entity, tagRepository);
                            log.debug("Updating existing post with postid: {}", parsed.postId());
                        } catch (Exception e) {
                            log.warn("Failed to update post entity for postId: {}", parsed.postId(), e);
                            return null;
                        }
                    } else {
                        // Create new entity
                        try {
                            entity = mapper.convert(parsed.request(), tagRepository);
                            String authorEmail = parsed.request().email();
                            AuthorEntity author = null;
                            try {
                                if (authorRepository.existsByEmailIgnoreCase(authorEmail)) {
                                    author = authorRepository.getReferenceByEmail(authorEmail);
                                }
                            } catch (Exception ex) {
                                // Fallback to a safe lookup if reference retrieval fails
                                author = authorRepository
                                        .findByEmailIgnoreCase(authorEmail)
                                        .orElse(null);
                            }
                            entity.setAuthorEntity(author);
                            log.debug("Creating new post with postRefId: {}", parsed.postId());
                        } catch (Exception e) {
                            log.warn("Failed to create post entity for postId: {}", parsed.postId(), e);
                            return null;
                        }
                    }
                    return entity;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Step 4: Save all (both new and updated)
        if (!entitiesToSave.isEmpty()) {
            try {
                postRepository.saveAll(entitiesToSave);
                log.debug(
                        "Persisted batch of {} post entities ({} updates, {} inserts)",
                        entitiesToSave.size(),
                        existingPosts.size(),
                        entitiesToSave.size() - existingPosts.size());
            } catch (Exception e) {
                log.error("Failed to persist batch of {} post entities", entitiesToSave.size(), e);
                throw e;
            }
        }
    }

    @Override
    public void processDeletes(List<String> keys) {
        if (keys.isEmpty()) return;
        try {
            long deletedRows = postRepository.deleteByPostRefIdIn(
                    keys.stream().map(Long::valueOf).toList());
            log.debug("Deleted rows :{} post entity for keys :{}", deletedRows, keys);
        } catch (Exception e) {
            log.warn("Failed to batch delete post entities for keys: {}", keys, e);
        }
    }

    @Override
    public String extractKey(String payload) {
        try {
            var node = jsonMapper.readTree(payload);
            String postId = node.path("postId").asString(null);
            if (postId == null || postId.isBlank()) {
                log.warn("Missing 'postId' field in post payload: {}", payload);
                return null;
            }
            return postId;
        } catch (Exception e) {
            log.warn("Failed to extract postId from post payload", e);
            return null;
        }
    }

    // Helper record to hold parsed data
    private record ParsedPost(String postId, NewPostRequest request) {}
}
