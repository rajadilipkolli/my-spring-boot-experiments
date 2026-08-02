package com.example.ultimatepostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ultimatepostgres.common.AbstractIntegrationTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class UltimatePostgresIntegrationTest extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        cacheService.evictAll();
        pubSubListener
                .getReceivedMessages()
                .clear(); // Reset messages, though getReceivedMessages returns a copy. Actually we might need a clear
        // method on the listener or just expect new messages at the end.
    }

    @Test
    void testCachePutGetEvictAndExpiry() throws Exception {
        cacheService.put("key1", objectMapper.readTree("""
                {"data": "value1"}"""), 10000);
        assertThat(cacheService.get("key1").orElseThrow().toString()).contains("\"data\":\"value1\"");

        cacheService.evict("key1");
        assertThat(cacheService.get("key1")).isEmpty();

        cacheService.put("key2", objectMapper.readTree("""
                {"data": "value2"}"""), 1); // 1 ms TTL
        Thread.sleep(10);
        assertThat(cacheService.get("key2")).isEmpty();

        cacheCleanupTask.cleanupExpiredEntries(); // should delete key2
    }

    @Test
    void testConcurrentQueueClaiming() throws Exception {
        // Enqueue 20 jobs
        for (int i = 0; i < 20; i++) {
            jobProducerService.enqueue(objectMapper.readTree("""
                    {"payload": "payload-%d"}""".formatted(i)), 0);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger totalProcessed = new AtomicInteger(0);

        Runnable worker = () -> {
            int processed = jobConsumerService.claimAndProcess(10);
            totalProcessed.addAndGet(processed);
            latch.countDown();
        };

        executor.submit(worker);
        executor.submit(worker);

        latch.await(5, TimeUnit.SECONDS);

        // Since SKIP LOCKED is used, both workers should be able to claim 10 disjoint jobs concurrently
        assertThat(totalProcessed.get()).isEqualTo(20);
    }

    @Test
    void testPubSub() {
        int initialMessages = pubSubListener.getReceivedMessages().size();

        pubSubPublisher.publish("test-message");

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> pubSubListener.getReceivedMessages().size() > initialMessages);

        assertThat(pubSubListener.getReceivedMessages()).contains("test-message");
    }

    @Test
    void testCombinedEndpoint() {
        int initialMessages = pubSubListener.getReceivedMessages().size();

        mockMvcTester
                .post()
                .uri("/api/pubsub/combined/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"data": "test-payload"}""")
                .assertThat()
                .hasStatusOk();

        assertThat(cacheService.get("combined:123").orElseThrow().toString()).contains("""
                "data":"test-payload\"""");

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> pubSubListener.getReceivedMessages().size() > initialMessages);

        assertThat(pubSubListener.getReceivedMessages()).contains("Combined operation executed for id: 123");
    }
}
