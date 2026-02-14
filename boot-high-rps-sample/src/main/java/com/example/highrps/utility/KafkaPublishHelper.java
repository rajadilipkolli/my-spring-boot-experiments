package com.example.highrps.utility;

import com.example.highrps.model.request.EventEnvelope;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;

/**
 * Helper utility for awaiting Kafka publish operations with consistent error
 * handling.
 */
public final class KafkaPublishHelper {

    private static final Logger log = LoggerFactory.getLogger(KafkaPublishHelper.class);

    private KafkaPublishHelper() {
        // Utility class
    }

    /**
     * Wait for a Kafka publish future to complete with timeout and error handling.
     *
     * @param future     the publish future
     * @param entityType the entity type being published (for logging)
     * @param key        the event key (for logging)
     * @param timeoutMs  timeout in milliseconds
     * @throws IllegalStateException if publish fails, times out, or is interrupted
     */
    public static void awaitPublish(
            CompletableFuture<SendResult<String, EventEnvelope>> future,
            String entityType,
            String key,
            long timeoutMs) {
        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            // Attempt to cancel the send if possible and surface a clear error
            try {
                future.cancel(true);
            } catch (Exception cancelEx) {
                log.warn("Failed to cancel publish future after timeout for {} key {}", entityType, key, cancelEx);
            }
            log.error("Timed out waiting for Kafka publish for {} key {} after {} ms", entityType, key, timeoutMs, te);
            throw new IllegalStateException("Timed out publishing " + entityType + " event for key " + key, te);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for Kafka publish for {} key {}", entityType, key, ie);
            throw new IllegalStateException("Interrupted while publishing " + entityType + " event for key " + key, ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause() == null ? ee : ee.getCause();
            log.error("Failed to publish {} envelope for key {}", entityType, key, cause);
            throw new IllegalStateException("Failed to publish " + entityType + " event for key " + key, cause);
        } catch (Exception ex) {
            log.error("Unexpected error while publishing {} envelope for key {}", entityType, key, ex);
            throw new IllegalStateException("Failed to publish " + entityType + " event for key " + key, ex);
        }
    }
}
