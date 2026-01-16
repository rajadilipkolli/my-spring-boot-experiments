package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.model.response.PostResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PostControllerIT extends AbstractIntegrationTest {

    @Test
    void createPost() {
        mockMvcTester
                .post()
                .content("""
                        {
                          "title": "High RPS with Spring Boot",
                          "content": "This is a sample post content.",
                          "email": "junit@email.com"
                        }
                        """)
                .uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .containsHeader("Location");
    }

    @Test
    void getPostByTitleNotFound() {
        mockMvcTester
                .get()
                .uri("/api/posts/non-existent-title")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void deletePostRemovesResources() {
        String title = "delete-me";

        // 1) Create a post
        mockMvcTester
                .post()
                .uri("/api/posts")
                .content("""
          {
            "title": "delete-me",
            "content": "Will be deleted",
            "email": "test@local.com"
          }
          """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        // Ensure caches/redis are populated by hitting GET (which populates local cache and redis)
        mockMvcTester
                .get()
                .uri("/api/posts/" + title)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse.title()).isEqualTo(title);
                    assertThat(postResponse.content()).isEqualTo("Will be deleted");
                    assertThat(postResponse.published()).isFalse();
                    assertThat(postResponse.publishedAt()).isNull();
                    assertThat(postResponse.tags()).isNull();
                    assertThat(postResponse.details()).isNull();
                });

        // Assert local cache and redis have the key
        String redisKey = "posts:" + title;
        String cached = localCache.getIfPresent(title);
        assertThat(cached).isNotNull();
        // as redis will take a short moment to be populated due to async nature, it should go through kafka and in
        // AggregatesToRedisListener value is set
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNull();

        // 2) Update the post via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/posts/" + title)
                .content("""
                        {
                          "title": "delete-me",
                          "content": "Updated content before delete",
                          "email": "test@local.com"
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(resp -> assertThat(resp.content()).isEqualTo("Updated content before delete"));

        // Verify caches updated with new content
        String cachedAfter = localCache.getIfPresent(title);
        assertThat(cachedAfter).isNotNull();
        assertThat(localCache.getIfPresent(title)).contains("Updated content before delete");
        // as redis will take a short moment to be populated due to async nature, it should go through kafka and in
        // AggregatesToRedisListener value is set
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNull();

        // 3) Delete the post
        mockMvcTester
                .delete()
                .uri("/api/posts/" + title)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // 4) Subsequent GET should return 404
        mockMvcTester
                .get()
                .uri("/api/posts/" + title)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(title)).isNull();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNull();
    }
}
