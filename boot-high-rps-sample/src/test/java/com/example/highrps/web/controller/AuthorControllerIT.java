package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.model.response.AuthorResponse;
import java.time.Duration;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class AuthorControllerIT extends AbstractIntegrationTest {

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
                });

        // Assert local cache has the key (redis may be populated asynchronously)
        String cached = localCache.getIfPresent(emailKey);
        assertThat(cached).isNotNull();
        AuthorResponse cachedResponse = AuthorResponse.fromJson(cached);
        assertThat(cachedResponse.middleName()).isNull();
        String redisString = redisTemplate.opsForValue().get("authors:" + emailKey);
        assertThat(redisString).isNotNull();
        cachedResponse = AuthorResponse.fromJson(redisString);
        assertThat(cachedResponse.middleName()).isNull();

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
                });

        // Verify local cache updated with new content
        String cachedAfter = localCache.getIfPresent(emailKey);
        assertThat(cachedAfter).isNotNull();
        AuthorResponse cachedAfterResponse = AuthorResponse.fromJson(cachedAfter);
        assertThat(cachedAfterResponse.middleName()).isEqualTo("IT");
        assertThat(cachedAfterResponse.firstName()).isEqualTo("junit");
        String redisAfterString = redisTemplate.opsForValue().get("authors:" + emailKey);
        assertThat(redisAfterString).isNotNull();
        AuthorResponse redisAfterResponse = AuthorResponse.fromJson(redisAfterString);
        assertThat(redisAfterResponse.middleName()).isEqualTo("IT");
        assertThat(redisAfterResponse.firstName()).isEqualTo("junit");

        // 3) Delete the author via API
        mockMvcTester
                .delete()
                .uri("/api/author/" + email)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(emailKey)).isNull();
        assertThat(redisTemplate.opsForValue().get("authors:" + emailKey)).isNull();
        assertThat(redisTemplate.opsForValue().get("deleted:authors:" + emailKey))
                .isNotNull();

        // 4) Subsequent GET should return 404
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .conditionEvaluationListener(condition -> {
                    // Uncomment for debugging pipeline failures
                    // System.out.println("Polling attempt: " + condition.getElapsedTimeInMS() + "ms");
                })
                .untilAsserted(() -> mockMvcTester
                        .get()
                        .uri("/api/author/" + email)
                        .exchange()
                        .assertThat()
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .hasContentType(MediaType.APPLICATION_PROBLEM_JSON));

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(emailKey)).isNull();
        assertThat(redisTemplate.hasKey("authors:" + emailKey)).isFalse();
    }
}
