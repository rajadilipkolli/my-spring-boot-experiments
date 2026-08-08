package com.example.ultimateredis.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import com.example.ultimateredis.model.AddRedisRequest;
import com.example.ultimateredis.model.GenericResponse;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisControllerTest extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void addRedisKeyValue() {
        AddRedisRequest addRedisRequest = new AddRedisRequest("junit", "JunitValue", 1);
        this.mockMvcTester
                .post()
                .uri("/v1/redis/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(addRedisRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo(true));

        // Add more test keys for pattern tests
        addRedisRequest = new AddRedisRequest("test:key1", "value1", 5);
        this.mockMvcTester
                .post()
                .uri("/v1/redis/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(addRedisRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        addRedisRequest = new AddRedisRequest("test:key2", "value2", 5);
        this.mockMvcTester
                .post()
                .uri("/v1/redis/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(addRedisRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    @Order(2)
    void getFromCache() {
        this.mockMvcTester
                .get()
                .uri("/v1/redis")
                .param("key", "junit")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo("JunitValue"));
    }

    @Test
    @Order(3)
    void getKeysByPattern() {
        this.mockMvcTester
                .get()
                .uri("/v1/redis/keys")
                .param("pattern", "test:*")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> keys = (ArrayList<String>) response.response();
                    assertThat(keys).containsAnyOf("app:v1:test:key1", "app:v1:test:key2");
                    assertThat(keys).size().isGreaterThanOrEqualTo(2);
                });
    }

    @Test
    @Order(4)
    void deleteKeysByPattern() {
        // Delete by pattern
        this.mockMvcTester
                .delete()
                .uri("/v1/redis/keys")
                .param("pattern", "test:*")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo(true));

        // Verify keys are deleted
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> this.mockMvcTester
                        .get()
                        .uri("/v1/redis/keys")
                        .param("pattern", "test:*")
                        .assertThat()
                        .hasStatusOk()
                        .bodyJson()
                        .convertTo(GenericResponse.class)
                        .satisfies(response -> {
                            @SuppressWarnings("unchecked")
                            ArrayList<String> keys = (ArrayList<String>) response.response();
                            assertThat(keys).isEmpty();
                        }));
    }

    @Test
    @Order(5)
    void expireFromCache() {
        // Manually speed up the expiry for the test so we don't wait 1 minute
        stringRedisTemplate.opsForValue().set("app:v1:junittimeout", "some-value");
        Boolean expired = stringRedisTemplate.expire("app:v1:junittimeout", 2, TimeUnit.SECONDS);
        assertThat(expired).isTrue();

        await().pollDelay(Duration.ofSeconds(1))
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> this.mockMvcTester
                        .get()
                        .uri("/v1/redis")
                        .param("key", "junittimeout")
                        .assertThat()
                        .hasStatusOk()
                        .hasContentType(MediaType.APPLICATION_JSON)
                        .bodyJson()
                        .convertTo(GenericResponse.class)
                        .satisfies(response -> assertThat(response.response()).isNull()));
    }

    @Test
    @Order(6)
    void testCasAndDigest() {
        String casKey = "cas-test-key";

        // 1. Initial setup using CAS SET-IFDNE (this writes raw bytes to Redis)
        // Since the key doesn't exist, we can use a dummy 16-hex character digest
        // If the key doesn't exist, IFDNE creates it in Redis 8.4.
        this.mockMvcTester
                .post()
                .uri("/v1/redis/cas/set-ifdne")
                .param("key", casKey)
                .param("value", "initial-value")
                .param("expectedValue", "0123456789abcdef")
                .assertThat()
                .hasStatusOk();

        // 2. Digest (uses prefix internally, so we pass raw key)
        this.mockMvcTester
                .get()
                .uri("/v1/redis/digest")
                .param("key", casKey)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isNotNull());

        // 3. CAS SET-IFEQ (now we know exactly what is in Redis: raw bytes "initial-value")
        this.mockMvcTester
                .post()
                .uri("/v1/redis/cas/set-ifeq")
                .param("key", casKey)
                .param("value", "new-value")
                .param("expectedValue", "initial-value")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo(true));

        // 4. CAS SET-IFDNE
        this.mockMvcTester
                .post()
                .uri("/v1/redis/cas/set-ifdne")
                .param("key", casKey)
                .param("value", "another-value")
                .param("expectedValue", "0123456789abcdef") // Dummy 16-hex char digest
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> assertThat(response.response()).isEqualTo(true));
    }

    @Test
    @Order(7)
    void testMetricsExposed() {
        // Capture baseline
        Timer timer = meterRegistry
                .find("redis.operation")
                .tag("method", "setIfEqual")
                .timer();
        long baselineTimerCount = timer != null ? timer.count() : 0;

        io.micrometer.core.instrument.Counter counter = meterRegistry
                .find("redis.operations")
                .tag("method", "setIfEqual")
                .tag("outcome", "success")
                .counter();
        double baselineCounterCount = counter != null ? counter.count() : 0;

        // Execute a successful setIfEqual
        this.mockMvcTester
                .post()
                .uri("/v1/redis/cas/set-ifeq")
                .param("key", "cas-test-key")
                .param("value", "another-value")
                .param("expectedValue", "new-value") // From previous test
                .assertThat()
                .hasStatusOk();

        // Wait briefly if needed for metrics to flush
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(meterRegistry
                            .find("redis.operation")
                            .tag("method", "setIfEqual")
                            .timer())
                    .isNotNull()
                    .satisfies(t -> assertThat(t.count()).isGreaterThan(baselineTimerCount));

            // Check that the counter for success operations was recorded
            assertThat(meterRegistry
                            .find("redis.operations")
                            .tag("method", "setIfEqual")
                            .tag("outcome", "success")
                            .counter())
                    .isNotNull()
                    .satisfies(c -> assertThat(c.count()).isGreaterThan(baselineCounterCount));
        });
    }
}
