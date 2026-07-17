package com.example.highrps.post.batch;

import com.example.highrps.author.domain.AuthorEntity;
import com.example.highrps.author.domain.AuthorRepository;
import com.example.highrps.infrastructure.kafka.batch.EntityBatchProcessor;
import com.example.highrps.infrastructure.redis.DeletionMarkerHandler;
import com.example.highrps.post.domain.PostEntity;
import com.example.highrps.post.domain.PostRepository;
import com.example.highrps.post.domain.TagEntity;
import com.example.highrps.post.domain.TagRepository;
import com.example.highrps.post.domain.TagResponse;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.post.mapper.NewPostRequestToPostEntityMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final AuthorRepository authorRepository;
    private final DeletionMarkerHandler deletionMarkerHandler;

    public PostBatchProcessor(
            NewPostRequestToPostEntityMapper mapper,
            PostRepository postRepository,
            TagRepository tagRepository,
            JsonMapper jsonMapper,
            AuthorRepository authorRepository,
            DeletionMarkerHandler deletionMarkerHandler) {
        this.mapper = mapper;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.jsonMapper = jsonMapper;
        this.authorRepository = authorRepository;
        this.deletionMarkerHandler = deletionMarkerHandler;
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
                        if (deletionMarkerHandler.isDeleted("post", postId)) {
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
                .map(parsedPost -> Long.valueOf(parsedPost.postId()))
                .toList();

        List<PostEntity> existingPosts = postRepository.findByPostRefIdIn(postIds);

        Map<Long, PostEntity> existingByPostId = existingPosts.stream()
                .collect(Collectors.toMap(PostEntity::getPostRefId, Function.identity(), (e1, e2) -> e1));

        // Step 2.1: Bulk fetch authors
        List<String> emails = parsedPosts.stream()
                .map(p -> p.request().email())
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();

        Map<String, AuthorEntity> authorByEmail = authorRepository.findByEmailInAllIgnoreCase(emails).stream()
                .collect(
                        Collectors.toMap(a -> a.getEmail().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));

        // Step 2.2: Bulk fetch and create tags
        Map<String, TagResponse> tagRequestsByName = parsedPosts.stream()
                .flatMap(p -> {
                    var tags = p.request().tags();
                    return tags != null ? tags.stream() : Stream.<TagResponse>empty();
                })
                .filter(t -> t != null && t.tagName() != null && !t.tagName().isBlank())
                .collect(Collectors.toMap(t -> t.tagName().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));

        List<String> tagNames = new ArrayList<>(tagRequestsByName.keySet());

        Map<String, TagEntity> tagMap = new HashMap<>();
        if (!tagNames.isEmpty()) {
            List<TagEntity> existingTags = tagRepository.findByTagNameInAllIgnoreCase(tagNames);
            existingTags.forEach(t -> tagMap.put(t.getTagName().toLowerCase(Locale.ROOT), t));

            List<TagEntity> newTags = tagNames.stream()
                    .filter(name -> !tagMap.containsKey(name))
                    .map(name -> {
                        TagResponse tr = tagRequestsByName.get(name);
                        TagEntity tagEntity =
                                new TagEntity().setTagName(tr.tagName()).setTagDescription(tr.tagDescription());
                        tagEntity.setCreatedAt(LocalDateTime.now());
                        return tagEntity;
                    })
                    .toList();

            if (!newTags.isEmpty()) {
                tagRepository
                        .saveAll(newTags)
                        .forEach(t -> tagMap.put(t.getTagName().toLowerCase(Locale.ROOT), t));
            }
        }

        // Step 3: Process each post - update existing or create new
        List<PostEntity> entitiesToSave = parsedPosts.stream()
                .map(parsed -> {
                    PostEntity entity = existingByPostId.get(Long.valueOf(parsed.postId()));
                    if (entity != null) {
                        // Update existing entity
                        try {
                            mapper.updatePostEntity(parsed.request(), entity, tagMap);
                            log.debug("Updating existing post with postid: {}", parsed.postId());
                        } catch (Exception e) {
                            log.error("Failed to update post entity for postId: {}", parsed.postId(), e);
                            throw new RuntimeException(
                                    "Failed to update post entity for postId: " + parsed.postId(), e);
                        }
                    } else {
                        // Create new entity
                        try {
                            entity = mapper.convert(parsed.request(), tagMap);
                            String authorEmail = parsed.request().email();
                            AuthorEntity author = null;
                            if (authorEmail != null) {
                                author = authorByEmail.get(authorEmail.toLowerCase(Locale.ROOT));
                            }
                            entity.setAuthorEntity(author);
                            log.debug("Creating new post with postRefId: {}", parsed.postId());
                        } catch (Exception e) {
                            log.error("Failed to create post entity for postId: {}", parsed.postId(), e);
                            throw new RuntimeException(
                                    "Failed to create post entity for postId: " + parsed.postId(), e);
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
