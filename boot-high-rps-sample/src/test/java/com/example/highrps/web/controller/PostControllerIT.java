package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.common.AbstractIntegrationTest;
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
                .hasStatus(HttpStatus.NOT_FOUND);
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
        mockMvcTester.get().uri("/api/posts/" + title).exchange().assertThat().hasStatus(HttpStatus.OK);

        // Assert local cache and redis have the key
        String redisKey = "posts:" + title;
        String cached = localCache.getIfPresent(title);
        assertThat(cached).isNotNull();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNotNull();

        // 2) Delete the post
        mockMvcTester
                .delete()
                .uri("/api/posts/" + title)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // 3) Subsequent GET should return 404
        mockMvcTester.get().uri("/api/posts/" + title).exchange().assertThat().hasStatus(HttpStatus.NOT_FOUND);

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(title)).isNull();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNull();
    }
}
