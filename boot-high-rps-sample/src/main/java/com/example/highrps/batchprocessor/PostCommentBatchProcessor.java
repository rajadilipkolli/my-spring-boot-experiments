package com.example.highrps.batchprocessor;

import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.entities.PostEntity;
import com.example.highrps.postcomment.domain.PostCommentResult;
import com.example.highrps.repository.jpa.PostCommentRepository;
import com.example.highrps.repository.jpa.PostRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PostCommentBatchProcessor implements EntityBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(PostCommentBatchProcessor.class);

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final JsonMapper jsonMapper;
    private final RedisTemplate<String, String> redis;

    public PostCommentBatchProcessor(
            PostCommentRepository postCommentRepository,
            PostRepository postRepository,
            JsonMapper jsonMapper,
            RedisTemplate<String, String> redis) {
        this.postCommentRepository = postCommentRepository;
        this.postRepository = postRepository;
        this.jsonMapper = jsonMapper;
        this.redis = redis;
    }

    @Override
    public String getEntityType() {
        return "post-comment";
    }

    @Override
    @Transactional
    public void processUpserts(List<String> payloads) {
        // Step 1: Parse payloads and extract IDs
        List<ParsedComment> parsedComments = payloads.stream()
                .map(payload -> {
                    String cacheKey = extractKey(payload);
                    if (cacheKey != null) {
                        // Skip if tombstone exists
                        Boolean deleted = redis.hasKey("deleted:post-comments:" + cacheKey);
                        if (Boolean.TRUE.equals(deleted)) {
                            log.debug("Skipping upsert for comment {} because recent tombstone present", cacheKey);
                            return null;
                        }
                    }
                    try {
                        PostCommentResult result = jsonMapper.readValue(payload, PostCommentResult.class);
                        return new ParsedComment(result.commentId(), result.postId(), result);
                    } catch (Exception e) {
                        log.warn("Failed to map post comment payload to result: {}", payload, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(p -> p.commentId() != null && p.postId() != null)
                .toList();

        if (parsedComments.isEmpty()) {
            return;
        }

        // Step 2: Extract all comment IDs and fetch existing comments from DB
        List<Long> commentIds =
                parsedComments.stream().map(ParsedComment::commentId).collect(Collectors.toList());

        List<PostCommentEntity> existingComments = postCommentRepository.findByCommentRefIdIn(commentIds);

        Map<Long, PostCommentEntity> existingById = existingComments.stream()
                .collect(Collectors.toMap(PostCommentEntity::getCommentRefId, Function.identity(), (e1, e2) -> e1));

        // Step 3: Process each comment - update existing or create new
        List<PostCommentEntity> entitiesToSave = parsedComments.stream()
                .map(parsed -> {
                    PostCommentEntity entity = existingById.get(parsed.commentId());
                    if (entity != null) {
                        // Update existing entity
                        try {
                            updateCommentEntity(parsed.result(), entity);
                            log.debug("Updating existing comment with id: {}", parsed.commentId());
                        } catch (Exception e) {
                            log.warn("Failed to update comment entity for id: {}", parsed.commentId(), e);
                            return null;
                        }
                    } else {
                        // Create new entity
                        try {
                            entity = createCommentEntity(parsed.result());
                            log.debug("Creating new comment with id: {}", parsed.commentId());
                        } catch (Exception e) {
                            log.warn("Failed to create comment entity for id: {}", parsed.commentId(), e);
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
                postCommentRepository.saveAll(entitiesToSave);
                log.debug(
                        "Persisted batch of {} comment entities ({} updates, {} inserts)",
                        entitiesToSave.size(),
                        existingComments.size(),
                        entitiesToSave.size() - existingComments.size());
            } catch (Exception e) {
                log.error("Failed to persist batch of {} comment entities", entitiesToSave.size(), e);
                throw e;
            }
        }
    }

    @Override
    public void processDeletes(List<String> keys) {
        if (keys.isEmpty()) return;
        try {
            List<Long> cacheKeysList = keys.stream()
                    .map(s -> s.split(":"))
                    .filter(parts -> parts.length == 2)
                    .map(parts -> {
                        try {
                            return Long.parseLong(parts[1]);
                        } catch (NumberFormatException e) {
                            log.warn("Failed to parse cache key: {}", String.join(":", parts), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            try {
                long deletedRows = postCommentRepository.deleteByCommentRefIdIn(cacheKeysList);
                log.debug("Deleted : {} comment entity for ids: {}", deletedRows, cacheKeysList);
            } catch (Exception e) {
                log.warn("Failed to delete comment entity for ids: {}", cacheKeysList, e);
            }
        } catch (Exception e) {
            log.warn("Failed to batch delete comment entities for keys: {}", keys, e);
        }
    }

    @Override
    public String extractKey(String payload) {
        try {
            var node = jsonMapper.readTree(payload);
            long commentId = node.path("commentId").asLong(-1L);
            long postId = node.path("postId").asLong(-1L);
            if (commentId == -1L || postId == -1L) {
                log.warn("Missing 'commentId' or 'postId' field in comment payload: {}", payload);
                return null;
            }
            return postId + ":" + commentId;
        } catch (Exception e) {
            log.warn("Failed to extract key from comment payload", e);
            return null;
        }
    }

    private void updateCommentEntity(PostCommentResult result, PostCommentEntity entity) {
        entity.setTitle(result.title());
        entity.setContent(result.content());
        entity.setPublished(result.published());
        entity.setPublishedAt(result.publishedAt());
        entity.setModifiedAt(result.modifiedAt());
    }

    private PostCommentEntity createCommentEntity(PostCommentResult result) {
        Optional<PostEntity> postExists = postRepository.findByPostRefId(result.postId());
        if (postExists.isEmpty()) {
            log.warn("Cannot create comment for non-existing post with id: {}", result.postId());
            throw new IllegalStateException("Post with id " + result.postId() + " does not exist");
        }
        PostCommentEntity entity = new PostCommentEntity(result.title(), result.content(), postExists.get());
        entity.setCommentRefId(result.commentId());
        entity.setPublished(result.published());
        entity.setPublishedAt(result.publishedAt());
        entity.setCreatedAt(result.createdAt());
        entity.setModifiedAt(result.modifiedAt());
        return entity;
    }

    // Helper record to hold parsed data
    private record ParsedComment(Long commentId, Long postId, PostCommentResult result) {}
}
