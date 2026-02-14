package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.model.response.AuthorResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AuthorControllerIT extends AbstractIntegrationTest {

    @Test
    void crudAuthorResourcesAPICheck() {
        String email = "junitState@email.com";
        var emailKey = email.toLowerCase(Locale.ROOT);

        // 1) Create an author via API
        mockMvcTester
                .post()
                .uri("/api/author")
                .content("""
          {
            "firstName": "junitState",
            "lastName": "integration",
            "email": "junitState@email.com",
            "mobile": 1234567890
          }
          """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/author/" + email);

        // Ensure caches/redis are populated by hitting GET (which populates local cache and redis)
        mockMvcTester
                .get()
                .uri("/api/author/" + email)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(AuthorResponse.class)
                .satisfies(authorResponse -> {
                    assertThat(authorResponse.email()).isEqualTo(email);
                    assertThat(authorResponse.firstName()).isEqualTo("junitState");
                    assertThat(authorResponse.middleName()).isNull();
                    assertThat(authorResponse.lastName()).isEqualTo("integration");
                    assertThat(authorResponse.mobile()).isEqualTo(1234567890L);
                    assertThat(authorResponse.registeredAt()).isNull();
                    assertThat(authorResponse.createdAt())
                            .isNotNull()
                            .isInstanceOf(LocalDateTime.class)
                            .isBefore(LocalDateTime.now());
                    assertThat(authorResponse.modifiedAt()).isNull();
                });

        // Assert local cache has the key (redis may be populated asynchronously)
        String cached = localCache.getIfPresent(emailKey);
        assertThat(cached).isNotNull();
        AuthorResponse cachedResponse = AuthorResponse.fromJson(cached);
        assertThat(cachedResponse.middleName()).isNull();
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
                .convertTo(AuthorResponse.class)
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

        // Verify local cache updated with new content
        String cachedAfter = localCache.getIfPresent(emailKey);
        assertThat(cachedAfter).isNotNull();
        AuthorResponse cachedAfterResponse = AuthorResponse.fromJson(cachedAfter);
        assertThat(cachedAfterResponse.middleName()).isEqualTo("IT");
        assertThat(cachedAfterResponse.firstName()).isEqualTo("junit");
        var updatedResponseFromRedis = authorRedisRepository.findById(emailKey).orElse(null);
        assertThat(updatedResponseFromRedis).isNotNull();
        assertThat(updatedResponseFromRedis.getMiddleName()).isEqualTo("IT");
        assertThat(updatedResponseFromRedis.getFirstName()).isEqualTo("junit");
        assertThat(updatedResponseFromRedis.getCreatedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(updatedResponseFromRedis.getModifiedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(updatedResponseFromRedis.getModifiedAt()).isAfter(updatedResponseFromRedis.getCreatedAt());

        // 3) Delete the author via API
        mockMvcTester
                .delete()
                .uri("/api/author/" + email)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(emailKey)).isNull();
        var redisAfterDelete = authorRedisRepository.findById(emailKey);
        assertThat(redisAfterDelete).isEmpty();
        assertThat(redisTemplate.opsForValue().get("deleted:authors:" + emailKey))
                .isNotNull()
                .isEqualTo("1");

        // 4) Subsequent GET should return 404
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> mockMvcTester
                        .get()
                        .uri("/api/author/" + email)
                        .exchange()
                        .assertThat()
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .hasContentType(MediaType.APPLICATION_PROBLEM_JSON));

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(emailKey)).isNull();

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
    }
}
