package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.model.response.AuthorResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class AuthorControllerIT extends AbstractIntegrationTest {

    @Test
    void crudAuthorResourcesAPICheck() {
        String email = "junitIt@email.com";

        // 1) Create an author via API
        mockMvcTester
                .post()
                .uri("/api/author")
                .content("""
          {
            "firstName": "junit",
            "lastName": "integration",
            "email": "junitIt@email.com",
            "mobile": 1234567890
          }
          """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

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
                    assertThat(authorResponse.firstName()).isEqualTo("junit");
                    assertThat(authorResponse.middleName()).isNull();
                    assertThat(authorResponse.lastName()).isEqualTo("integration");
                    assertThat(authorResponse.mobile()).isEqualTo(1234567890L);
                    assertThat(authorResponse.registeredAt()).isNull();
                });

        // Assert local cache has the key (redis may be populated asynchronously)
        String cached = localCache.getIfPresent(email);
        assertThat(cached).isNotNull();
        assertThat(cached).doesNotContain("IT");

        // 2) Update the author via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/author/" + email)
                .content("""
                        {
                            "firstName": "junit",
                            "middleName": "IT",
                            "lastName": "integration",
                            "email": "junitIt@email.com",
                            "mobile": 1234567890
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(AuthorResponse.class)
                .satisfies(resp -> assertThat(resp.middleName()).isEqualTo("IT"));

        // Verify caches updated with new content
        String cachedAfter = localCache.getIfPresent(email);
        assertThat(cachedAfter).isNotNull();
        assertThat(cachedAfter).contains("IT");

        // 3) Delete the author via API
        mockMvcTester
                .delete()
                .uri("/api/author/" + email)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        assertThat(localCache.getIfPresent(email)).isNull();

        // 4) Subsequent GET should return 404
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> mockMvcTester
                        .get()
                        .uri("/api/author/" + email)
                        .exchange()
                        .assertThat()
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .hasContentType(MediaType.APPLICATION_PROBLEM_JSON));

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(email)).isNull();
    }
}
