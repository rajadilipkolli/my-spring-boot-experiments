package com.example.highrps.batchprocessor;

import com.example.highrps.mapper.AuthorRequestToEntityMapper;
import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.repository.AuthorRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AuthorBatchProcessor implements EntityBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(AuthorBatchProcessor.class);

    private final AuthorRequestToEntityMapper mapper;
    private final AuthorRepository authorRepository;
    private final JsonMapper jsonMapper;

    public AuthorBatchProcessor(
            AuthorRequestToEntityMapper mapper, AuthorRepository authorRepository, JsonMapper jsonMapper) {
        this.mapper = mapper;
        this.authorRepository = authorRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public String getEntityType() {
        return "author";
    }

    @Override
    public void processUpserts(List<String> payloads) {
        var entities = payloads.stream()
                .map(payload -> {
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
        keys.forEach(email -> {
            try {
                if (authorRepository.existsByEmail(email)) {
                    authorRepository.deleteByEmail(email);
                    log.debug("Deleted author entity for email: {}", email);
                }
            } catch (Exception e) {
                log.warn("Failed to delete author entity for email: {}", email, e);
            }
        });
    }

    @Override
    public String extractKey(String payload) {
        try {
            var node = jsonMapper.readTree(payload);
            return node.get("email").asString();
        } catch (Exception e) {
            log.warn("Failed to extract email from author payload", e);
            return null;
        }
    }
}
