package com.example.highrps.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Base template class for command services.
 * Encapsulates the complex asynchronous pipeline for publishing domain events
 * to Kafka and subsequently locking the aggregate to perform cache updates or tombstoning.
 */
public abstract class AbstractCommandService {

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandService.class);
    private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    protected final KafkaTemplate<String, Object> kafkaTemplate;
    protected final AggregateOperationQueue operationQueue;

    protected AbstractCommandService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.operationQueue = new AggregateOperationQueue();
    }

    /**
     * Executes a command pipeline.
     *
     * @param topic              The Kafka topic to publish to
     * @param kafkaKey           The key for the Kafka message
     * @param lockKey            The aggregate key used to lock operations in the AggregateOperationQueue
     * @param event              The domain event to publish
     * @param result             The command result to return
     * @param postPublishAction  The action to run after successful publish (e.g., updating caches)
     * @param actionLogName      The name of the action for logging (e.g., "create post")
     * @param entityLogName      The name of the entity for logging (e.g., "Post")
     * @param <T>                The type of the result
     * @return CompletableFuture resolving to the command result
     */
    protected <T> CompletableFuture<T> executeCommand(
            String topic,
            String kafkaKey,
            String lockKey,
            Object event,
            T result,
            Runnable postPublishAction,
            String actionLogName,
            String entityLogName) {

        return kafkaTemplate
                .send(topic, kafkaKey, event)
                .handleAsync(
                        (res, err) -> {
                            if (err != null) {
                                log.error("Failed to publish {} event for key: {}", actionLogName, kafkaKey, err);
                                throw new RuntimeException("Failed to publish " + actionLogName + " event", err);
                            } else {
                                log.info("Successfully published {} event for key: {}", actionLogName, kafkaKey);
                                return result;
                            }
                        },
                        VIRTUAL_EXECUTOR)
                .thenComposeAsync(
                        cmdResult -> operationQueue
                                .enqueue(lockKey, () -> {
                                    postPublishAction.run();
                                    log.info("{} {} successfully: {}", entityLogName, actionLogName, lockKey);
                                    return CompletableFuture.completedFuture(null);
                                })
                                .thenApply(ignored -> cmdResult),
                        VIRTUAL_EXECUTOR);
    }
}
