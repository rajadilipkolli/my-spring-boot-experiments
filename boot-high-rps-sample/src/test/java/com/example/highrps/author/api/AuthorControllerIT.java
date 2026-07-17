package com.example.highrps.author.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.author.query.AuthorProjection;
import com.example.highrps.common.AbstractIntegrationTest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

class AuthorControllerIT extends AbstractIntegrationTest {

    @Test
    void crudAuthorResourcesAPICheck() {
        String email = "junitState-" + UUID.randomUUID() + "@email.com";
        var emailKey = email.toLowerCase(Locale.ROOT);

        // 1) Create an author via API
        mockMvcTester
                .post()
                .uri("/api/author")
                .content("""
          {
            "firstName": "junitState",
            "lastName": "integration",
            "email": "%s",
            "mobile": 1234567890
          }
          """.formatted(email))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/author/" + email);

        // 2) Ensure caches/redis are populated by hitting GET with retry (Awaitility)
        // for eventual consistency since Redis update is now asynchronous via Kafka
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    mockMvcTester
                            .get()
                            .uri("/api/author/" + email)
                            .exchange()
                            .assertThat()
                            .hasStatus(HttpStatus.OK)
                            .hasContentType(MediaType.APPLICATION_JSON)
                            .bodyJson()
                            .convertTo(AuthorProjection.class)
                            .satisfies(authorProjection -> {
                                assertThat(authorProjection.email()).isEqualTo(email.toLowerCase(Locale.ROOT));
                                assertThat(authorProjection.firstName()).isEqualTo("junitState");
                                assertThat(authorProjection.middleName()).isNull();
                                assertThat(authorProjection.lastName()).isEqualTo("integration");
                                assertThat(authorProjection.mobile()).isEqualTo(1234567890L);
                                assertThat(authorProjection.registeredAt()).isNull();
                                assertThat(authorProjection.createdAt())
                                        .isNotNull()
                                        .isInstanceOf(LocalDateTime.class)
                                        .isBefore(LocalDateTime.now());
                                assertThat(authorProjection.modifiedAt()).isNull();
                            });
                });

        // Assert local cache has the key (redis may be populated asynchronously)
        String cached = localCache.getIfPresent(emailKey);
        assertThat(cached).isNotNull();
        AuthorProjection cachedProjection = jsonMapper.readValue(cached, AuthorProjection.class);
        assertThat(cachedProjection.middleName()).isNull();
        var redisString = authorRedisRepository.findById(emailKey).orElse(null);
        assertThat(redisString).isNotNull();
        assertThat(redisString.getMiddleName()).isNull();
        assertThat(redisString.getCreatedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(redisString.getModifiedAt())
                .isNull(); // modifiedAt should be null on create as there is no update yet

        // 2) Update the author via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/author/" + email)
                .content("""
                        {
                            "firstName": "junit",
                            "middleName": "IT",
                            "lastName": "integration",
                            "email": "junitState@email.com",
                            "mobile": 1234567890
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(AuthorProjection.class)
                .satisfies(resp -> {
                    assertThat(resp.middleName()).isEqualTo("IT");
                    assertThat(resp.firstName()).isEqualTo("junit");
                    assertThat(resp.createdAt())
                            .isNotNull()
                            .isInstanceOf(LocalDateTime.class)
                            .isBefore(LocalDateTime.now());
                    assertThat(resp.modifiedAt())
                            .isNotNull()
                            .isInstanceOf(LocalDateTime.class)
                            .isBefore(LocalDateTime.now());
                });

        // Verify local cache updated with new content using Awaitility for consistency
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    String cachedAfter = localCache.getIfPresent(emailKey);
                    assertThat(cachedAfter).isNotNull();
                    AuthorProjection cachedAfterProjection = jsonMapper.readValue(cachedAfter, AuthorProjection.class);
                    assertThat(cachedAfterProjection.middleName()).isEqualTo("IT");
                    assertThat(cachedAfterProjection.firstName()).isEqualTo("junit");

                    var updatedResponseFromRedis =
                            authorRedisRepository.findById(emailKey).orElse(null);
                    assertThat(updatedResponseFromRedis).isNotNull();
                    assertThat(updatedResponseFromRedis.getMiddleName()).isEqualTo("IT");
                    assertThat(updatedResponseFromRedis.getFirstName()).isEqualTo("junit");
                    assertThat(updatedResponseFromRedis.getCreatedAt())
                            .isNotNull()
                            .isInstanceOf(LocalDateTime.class);
                    assertThat(updatedResponseFromRedis.getModifiedAt())
                            .isNotNull()
                            .isInstanceOf(LocalDateTime.class);
                    assertThat(updatedResponseFromRedis.getModifiedAt())
                            .isAfter(updatedResponseFromRedis.getCreatedAt());
                });

        // 3) Delete the author via API
        mockMvcTester
                .delete()
                .uri("/api/author/" + email)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // 4) Subsequent GET should return 404 immediately due to synchronous cache invalidation and tombstone marker
        mockMvcTester
                .get()
                .uri("/api/author/" + email)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        // Assert local cache and redis no longer have the key (using Awaitility)
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(localCache.getIfPresent(emailKey)).isNull();
                    var redisAfterDelete = authorRedisRepository.findById(emailKey);
                    assertThat(redisAfterDelete).isEmpty();
                    assertThat(redisTemplate.opsForValue().get("deleted:author:" + emailKey))
                            .isNotNull()
                            .isEqualTo("1");
                });

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(emailKey)).isNull();

        /*
        double count =
                this.meterRegistry.get("app.kafka.events.published").counter().count();
        assertThat(count).isGreaterThanOrEqualTo(2); // at least create and delete events should be published
        double deleteCount = this.meterRegistry
                .get("app.kafka.tombstones.published")
                .counter()
                .count();
        assertThat(deleteCount).isGreaterThanOrEqualTo(1); // at least 1 tombstone should be published for the delete
        double authorPublishedCount =
                this.meterRegistry.counter("authors.events.published").count();
        assertThat(authorPublishedCount)
                .isEqualTo(2); // create and update events should be published to the per-entity topic
        double authorTombstoneCount =
                this.meterRegistry.counter("authors.tombstones.published").count();
        assertThat(authorTombstoneCount)
                .isEqualTo(1); // at least 1 tombstone should be published to the per-entity topic for the delete
         */
    }

    @Test
    void shouldFallbackToKafkaStreamsWhenCachesAreMissed() {
        String email = "streams-fallback-" + UUID.randomUUID() + "@email.com";
        var emailKey = email.toLowerCase(Locale.ROOT);

        // 1) Create an author
        mockMvcTester
                .post()
                .uri("/api/author")
                .content("""
                        {
                          "firstName": "Streams",
                          "lastName": "Fallback",
                          "email": "%s",
                          "mobile": 1234567890
                        }
                        """.formatted(email))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        // Wait for it to be fully processed by Kafka Streams and written to Redis
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        assertThat(authorRedisRepository.findById(emailKey)).isPresent());

        // 2) Clear local cache and Redis
        localCache.invalidate(emailKey);
        authorRedisRepository.deleteById(emailKey);

        assertThat(localCache.getIfPresent(emailKey)).isNull();
        assertThat(authorRedisRepository.findById(emailKey)).isEmpty();

        // Stop the Kafka listener to prevent it from repopulating Redis asynchronously during the test,
        // which forces the GET request to strictly rely on the Kafka Streams state store fallback.
        KafkaListenerEndpointRegistry registry = applicationContext.getBean(KafkaListenerEndpointRegistry.class);
        MessageListenerContainer listenerContainer = registry != null
                ? registry.getListenerContainers().stream()
                        .filter(c -> "authors-redis-writer".equals(c.getGroupId())
                                || "authors-aggregates"
                                        .equals(c.getContainerProperties().getTopics()[0]))
                        .findFirst()
                        .orElse(null)
                : null;

        assertThat(listenerContainer)
                .as("Targeted Redis-writer listener container must be found")
                .isNotNull();
        listenerContainer.pause();

        try {
            // 3) GET request should fall back to Kafka Streams and succeed
            mockMvcTester
                    .get()
                    .uri("/api/author/" + email)
                    .exchange()
                    .assertThat()
                    .hasStatus(HttpStatus.OK)
                    .hasContentType(MediaType.APPLICATION_JSON)
                    .bodyJson()
                    .convertTo(AuthorProjection.class)
                    .satisfies(authorProjection -> {
                        assertThat(authorProjection.email()).isEqualTo(emailKey);
                        assertThat(authorProjection.firstName()).isEqualTo("Streams");
                    });

            // 4) Assert Redis is populated again by the fallback warm-up logic
            await().atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(() ->
                            assertThat(authorRedisRepository.findById(emailKey)).isPresent());

            // Assert local cache is also populated
            assertThat(localCache.getIfPresent(emailKey)).isNotNull();
        } finally {
            if (listenerContainer != null) {
                listenerContainer.resume();
            }
        }
    }
}
