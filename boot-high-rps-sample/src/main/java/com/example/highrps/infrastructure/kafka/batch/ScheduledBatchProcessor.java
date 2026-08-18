package com.example.highrps.infrastructure.kafka.batch;

import com.example.highrps.shared.config.AppProperties;
import com.example.highrps.shared.redis.DeletionMarkerHandler;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Generic batch processor that handles asynchronous persistence of entities from Redis queue.
 * Uses strategy pattern to delegate entity-specific operations to EntityBatchProcessor implementations.
 * Supports multiple entity types (posts, authors, etc.) with automatic routing based on entity metadata.
 */
@Component
public class ScheduledBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduledBatchProcessor.class);

    private final RedisTemplate<String, String> redis;
    private final JsonMapper jsonMapper;
    private final Map<String, EntityBatchProcessor> processorsByEntityType;
    private final DeletionMarkerHandler deletionMarkerHandler;

    private final String consumerName;
    private final MeterRegistry meterRegistry;
    private final AppProperties appProperties;

    private record QueueItem(String recordId, String payload) {}

    public ScheduledBatchProcessor(
            RedisTemplate<String, String> redis,
            JsonMapper jsonMapper,
            List<EntityBatchProcessor> processors,
            AppProperties appProperties,
            DeletionMarkerHandler deletionMarkerHandler,
            MeterRegistry meterRegistry) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
        this.appProperties = appProperties;
        this.deletionMarkerHandler = deletionMarkerHandler;
        this.meterRegistry = meterRegistry;
        this.consumerName = createConsumerName(getHostname(), UUID.randomUUID());

        // Build registry of processors by entity type
        this.processorsByEntityType =
                processors.stream().collect(Collectors.toMap(EntityBatchProcessor::getEntityType, p -> p));

        log.info(
                "Initialized ScheduledBatchProcessor with {} entity processors: {}",
                processorsByEntityType.size(),
                processorsByEntityType.keySet());
    }

    private static final String CONSUMER_GROUP = "batch-processor-group";

    static String createConsumerName(String hostname, UUID uuid) {
        return "processor-" + hostname + "-" + uuid;
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }

    static boolean isBusyGroupException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        String message = throwable.getMessage();
        if (message != null && message.toUpperCase(Locale.ROOT).contains("BUSYGROUP")) {
            return true;
        }
        return isBusyGroupException(throwable.getCause());
    }

    @PostConstruct
    public void init() {
        String queueKey = appProperties.getBatch().getQueueKey();
        try {
            if (!Boolean.TRUE.equals(redis.hasKey(queueKey))) {
                redis.opsForStream().add(queueKey, Map.of("_init", "true"));
            }
            redis.opsForStream().createGroup(queueKey, ReadOffset.from("0"), CONSUMER_GROUP);
        } catch (Exception e) {
            if (isBusyGroupException(e)) {
                log.info("Consumer group already exists: {}", CONSUMER_GROUP);
            } else {
                log.error(
                        "Failed to initialize Redis stream consumer group {} for queue {}",
                        CONSUMER_GROUP,
                        queueKey,
                        e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.batch.delay-ms}")
    @CircuitBreaker(name = "dbBatchWrites", fallbackMethod = "processBatchFallback")
    @Bulkhead(name = "dbBatchWrites", fallbackMethod = "processBatchFallback")
    public void processBatch() {
        claimPendingRecords();
        processRecords(ReadOffset.from("0"), true);
        // Then process new messages
        processRecords(ReadOffset.lastConsumed(), false);
    }

    @SuppressWarnings("unused")
    public void processBatchFallback(Throwable t) {
        log.warn("Scheduled batch processing skipped due to Resilience4j open circuit or full bulkhead", t);
    }

    private void claimPendingRecords() {
        String queueKey = appProperties.getBatch().getQueueKey();
        try {
            PendingMessagesSummary summary = redis.opsForStream().pending(queueKey, CONSUMER_GROUP);
            if (summary != null) {
                summary.getPendingMessagesPerConsumer().forEach((consumer, count) -> {
                    if (!consumer.equals(this.consumerName) && count > 0) {
                        PendingMessages pendingRecords = redis.opsForStream()
                                .pending(
                                        queueKey,
                                        Consumer.from(CONSUMER_GROUP, consumer),
                                        Range.unbounded(),
                                        appProperties.getBatch().getSize());

                        List<RecordId> recordIdsToClaim = pendingRecords.stream()
                                .filter(p -> p.getElapsedTimeSinceLastDelivery().toMillis() > 60000)
                                .map(PendingMessage::getId)
                                .toList();

                        if (!recordIdsToClaim.isEmpty()) {
                            log.info(
                                    "Claiming {} pending records from inactive consumer {}",
                                    recordIdsToClaim.size(),
                                    consumer);
                            redis.opsForStream()
                                    .claim(
                                            queueKey,
                                            CONSUMER_GROUP,
                                            this.consumerName,
                                            Duration.ofMinutes(1),
                                            recordIdsToClaim.toArray(new RecordId[0]));
                            if (recordIdsToClaim.size() == count) {
                                log.info(
                                        "Deleting inactive consumer {} after reclaiming all pending records", consumer);
                                redis.opsForStream().deleteConsumer(queueKey, Consumer.from(CONSUMER_GROUP, consumer));
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Failed to claim pending records from Redis Stream: {}", queueKey, e);
        }
    }

    private void processRecords(ReadOffset offset, boolean isPending) {
        boolean moreItems = true;
        int maxIterations = 100;
        int iterations = 0;
        ReadOffset currentOffset = offset;
        while (moreItems && iterations < maxIterations) {
            iterations++;
            List<MapRecord<String, Object, Object>> records;
            String queueKey = appProperties.getBatch().getQueueKey();
            int batchSize = appProperties.getBatch().getSize();
            try {
                records = redis.opsForStream()
                        .read(
                                Consumer.from(CONSUMER_GROUP, consumerName),
                                StreamReadOptions.empty().count(batchSize),
                                StreamOffset.create(queueKey, currentOffset));
            } catch (Exception e) {
                log.error("Failed to read from Redis Stream: {}", queueKey, e);
                break;
            }

            if (records == null || records.isEmpty()) {
                break;
            }

            List<QueueItem> items = records.stream()
                    .map(r -> {
                        Object val = r.getValue().get("payload");
                        return new QueueItem(r.getId().getValue(), val != null ? val.toString() : null);
                    })
                    .toList();

            List<String> ackIds = processItems(items);

            if (!ackIds.isEmpty()) {
                try {
                    redis.opsForStream().acknowledge(queueKey, CONSUMER_GROUP, ackIds.toArray(new String[0]));
                } catch (Exception e) {
                    log.error("Failed to XACK records in Redis Stream: {}", queueKey, e);
                }
            }

            if (isPending) {
                currentOffset =
                        ReadOffset.from(records.get(records.size() - 1).getId().getValue());
            }
            if (records.size() < batchSize) {
                moreItems = false;
            }
        }
    }

    private List<String> processItems(List<QueueItem> items) {
        Map<String, Map<String, PayloadOrTombstone>> groupedByEntityType = new HashMap<>();
        List<String> ackIds = new ArrayList<>();

        for (QueueItem item : items) {
            if (item.payload() == null || item.payload().equals("null")) {
                ackIds.add(item.recordId());
                continue;
            }

            try {
                var node = jsonMapper.readTree(item.payload());
                String entityType = node.has("__entity") ? node.get("__entity").asString() : null;
                boolean isDeleted =
                        node.has("__deleted") && node.get("__deleted").asBoolean(false);

                EntityBatchProcessor processor = processorsByEntityType.get(entityType);
                if (processor == null) {
                    log.warn("No processor found for entity type: {}", entityType);
                    moveToDlq(
                            "events:dlq",
                            item.payload(),
                            "no_processor_for_entity: " + entityType,
                            item.recordId(),
                            ackIds);
                    continue;
                }

                String key = processor.extractKey(item.payload());
                if (key == null) {
                    log.warn("Failed to extract key from payload for entity type: {}", entityType);
                    moveToDlq("events:dlq", item.payload(), "failed_to_extract_key", item.recordId(), ackIds);
                    continue;
                }

                // If this entity was deleted recently, skip any queued upsert that could resurrect it.
                // Tombstones still flow through normally.
                if (entityType != null && !isDeleted) {
                    if (deletionMarkerHandler.isDeleted(entityType, key)) {
                        log.debug(
                                "Skipping queued upsert because it is marked deleted: entity={}, key={}",
                                entityType,
                                key);
                        ackIds.add(item.recordId());
                        continue;
                    }
                }

                // Place into per-entity map, with tombstone taking precedence over payloads.
                var perKey = groupedByEntityType.computeIfAbsent(entityType, k -> new HashMap<>());
                PayloadOrTombstone existing = perKey.get(key);
                if (isDeleted) {
                    if (existing != null) {
                        ackIds.add(existing.recordId()); // Ack superseded
                    }
                    perKey.put(key, PayloadOrTombstone.tombstone(item.payload(), key, item.recordId()));
                } else {
                    if (existing != null) {
                        ackIds.add(existing.recordId()); // Ack superseded
                    }
                    perKey.put(key, PayloadOrTombstone.payload(item.payload(), key, item.recordId()));
                }
            } catch (Exception e) {
                log.warn("Failed to parse queued item: {}", item.payload(), e);
                moveToDlq("events:dlq", item.payload(), "parse_error: " + e.getMessage(), item.recordId(), ackIds);
            }
        }

        // Process each entity type's batch
        groupedByEntityType.forEach((entityType, payloadsByKey) -> {
            EntityBatchProcessor processor = processorsByEntityType.get(entityType);
            if (processor == null) return;

            List<PayloadOrTombstone> deletes = payloadsByKey.values().stream()
                    .filter(PayloadOrTombstone::isTombstone)
                    .toList();

            List<PayloadOrTombstone> upserts = payloadsByKey.values().stream()
                    .filter(p -> !p.isTombstone())
                    .toList();

            if (!upserts.isEmpty()) {
                try {
                    processor.processUpserts(
                            upserts.stream().map(PayloadOrTombstone::payload).toList());
                    ackIds.addAll(
                            upserts.stream().map(PayloadOrTombstone::recordId).toList());
                } catch (Exception e) {
                    log.warn("Batch upsert failed for entity type: {}", entityType, e);
                    ackIds.addAll(processUpsertsIndividually(processor, upserts));
                }
            }

            if (!deletes.isEmpty()) {
                try {
                    processor.processDeletes(
                            deletes.stream().map(PayloadOrTombstone::key).toList());
                    ackIds.addAll(
                            deletes.stream().map(PayloadOrTombstone::recordId).toList());
                } catch (Exception e) {
                    log.warn("Batch delete failed for entity type: {}", entityType, e);
                    ackIds.addAll(processDeletesIndividually(processor, deletes));
                }
            }
        });
        return ackIds;
    }

    private void moveToDlq(String queueKey, String payload, String reason, String recordId, List<String> ackIds) {
        try {
            Map<String, String> dlqMessage = new HashMap<>();
            if (payload != null) {
                dlqMessage.put("payload", payload);
            }
            dlqMessage.put("reason", reason);
            dlqMessage.put("originalRecordId", recordId);

            redis.opsForStream().add("events:dlq", dlqMessage);
            ackIds.add(recordId);
            log.info("Sidelined poisoned record {} to events:dlq. Reason: {}", recordId, reason);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to push to events:dlq stream for record {}", recordId, e);
        }
    }

    private List<String> processUpsertsIndividually(EntityBatchProcessor processor, List<PayloadOrTombstone> payloads) {
        String entityType = processor.getEntityType();
        List<String> ackIds = new ArrayList<>();

        for (PayloadOrTombstone p : payloads) {
            try {
                processor.processUpserts(List.of(p.payload()));
                ackIds.add(p.recordId());
            } catch (Exception e) {
                log.error("Failed individual upsert for {}, moving to DLQ", entityType, e);
                moveToDlq(
                        "events:dlq", p.payload(), "individual_upsert_failed: " + e.getMessage(), p.recordId(), ackIds);
            }
        }
        return ackIds;
    }

    private List<String> processDeletesIndividually(EntityBatchProcessor processor, List<PayloadOrTombstone> keys) {
        String entityType = processor.getEntityType();
        List<String> ackIds = new ArrayList<>();

        for (PayloadOrTombstone p : keys) {
            try {
                processor.processDeletes(List.of(p.key()));
                ackIds.add(p.recordId());
            } catch (Exception e) {
                log.error("Failed individual delete for {} key {}, moving to DLQ", entityType, p.key(), e);
                moveToDlq(
                        "events:dlq", p.payload(), "individual_delete_failed: " + e.getMessage(), p.recordId(), ackIds);
            }
        }
        return ackIds;
    }

    private record PayloadOrTombstone(String payload, String key, String recordId, boolean isTombstone) {
        static PayloadOrTombstone payload(String payload, String key, String recordId) {
            return new PayloadOrTombstone(payload, key, recordId, false);
        }

        static PayloadOrTombstone tombstone(String payload, String key, String recordId) {
            return new PayloadOrTombstone(payload, key, recordId, true);
        }
    }
}
