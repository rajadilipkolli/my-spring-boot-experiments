package com.example.highrps.batchprocessor;

import com.example.highrps.entities.AuthorEntity;
import com.example.highrps.mapper.AuthorRequestToEntityMapper;
import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.repository.jpa.AuthorRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
        // Step 1: Parse payloads and extract emails
        List<ParsedAuthor> parsedAuthors = payloads.stream()
                .map(payload -> {
                    String email = extractKey(payload);
                    if (email != null) {
                        // Skip if tombstone exists
                        Boolean deleted = redis.hasKey("deleted:authors:" + email);
                        if (Boolean.TRUE.equals(deleted)) {
                            log.debug("Skipping upsert for email {} because recent tombstone present", email);
                            return null;
                        }
                    }
                    try {
                        AuthorRequest req = jsonMapper.readValue(payload, AuthorRequest.class);
                        return new ParsedAuthor(email, req);
                    } catch (Exception e) {
                        log.warn("Failed to map author payload to entity: {}", payload, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (parsedAuthors.isEmpty()) {
            return;
        }

        // Step 2: Extract all emails and fetch existing authors from DB
        List<String> emails = parsedAuthors.stream()
                .map(ParsedAuthor::email)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<AuthorEntity> existingAuthors = authorRepository.findByEmailInAllIgnoreCase(emails);

        Map<String, AuthorEntity> existingByEmail = existingAuthors.stream()
                .collect(Collectors.toMap(a -> a.getEmail().toLowerCase(Locale.ROOT), Function.identity()));

        // Step 3: Process each author - update existing or create new
        List<AuthorEntity> entitiesToSave = parsedAuthors.stream()
                .map(parsed -> {
                    AuthorEntity entity = existingByEmail.get(parsed.email());
                    if (entity != null) {
                        // Update existing entity
                        try {
                            mapper.updateAuthorEntity(parsed.request(), entity);
                            log.debug("Updating existing author with email: {}", parsed.email());
                        } catch (Exception e) {
                            log.warn("Failed to update author entity for email: {}", parsed.email(), e);
                            return null;
                        }
                    } else {
                        // Create new entity
                        try {
                            entity = mapper.convert(parsed.request());
                            log.debug("Creating new author with email: {}", parsed.email());
                        } catch (Exception e) {
                            log.warn("Failed to create author entity for email: {}", parsed.email(), e);
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
                authorRepository.saveAll(entitiesToSave);
                log.debug(
                        "Persisted batch of {} author entities ({} updates, {} inserts)",
                        entitiesToSave.size(),
                        existingAuthors.size(),
                        entitiesToSave.size() - existingAuthors.size());
            } catch (Exception e) {
                log.error("Failed to persist batch of {} author entities", entitiesToSave.size(), e);
                throw e;
            }
        }
    }

    @Override
    public void processDeletes(List<String> keys) {
        if (keys.isEmpty()) return;
        try {
            List<String> lowerCaseKeys = keys.stream().map(String::toLowerCase).toList();
            long deletedRows = authorRepository.deleteByEmailInAllIgnoreCase(lowerCaseKeys);
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

    // Helper record to hold parsed data
    private record ParsedAuthor(String email, AuthorRequest request) {}
}
