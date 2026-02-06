package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.entities.PostRedis;
import com.example.highrps.model.response.PostResponse;
import java.time.Duration;
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
    void crudPostResourcesAPICheck() {
        String title = "sample-post";

        // 1) Create a post
        mockMvcTester
                .post()
                .uri("/api/posts")
                .content("""
          {
            "title": "sample-post",
            "content": "Will be deleted later",
            "email": "test1@local.com"
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
                    assertThat(postResponse.content()).isEqualTo("Will be deleted later");
                    assertThat(postResponse.published()).isFalse();
                    assertThat(postResponse.publishedAt()).isNull();
                    assertThat(postResponse.tags()).isNull();
                    assertThat(postResponse.details()).isNull();
                });

        // Assert local cache and redis have the key
        String redisKey = "posts:" + title;
        String cached = localCache.getIfPresent(title);
        assertThat(cached).isNotNull();
        // Redis may be populated synchronously or asynchronously depending on timing and listener implementation.
        // Make the test robust by awaiting eventual consistency.
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PostRedis value = postRedisRepository.findById(redisKey).orElse(null);
                    assertThat(value).isNotNull();
                    assertThat(value.getContent()).isEqualTo("Will be deleted later");
                });

        // 2) Update the post via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/posts/" + title)
                .content("""
                        {
                            "title": "sample-post",
                            "content": "Updated content before delete",
                            "email": "test@local.com",
                            "published": true,
                            "details": {
                                "detailsKey": "This is a summary",
                                "createdBy": "Some additional info"
                            }
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
        assertThat(cachedAfter).contains("Updated content before delete");
        // Redis may be updated asynchronously via Kafka; await the updated aggregate.
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PostRedis value = postRedisRepository.findById(redisKey).orElse(null);
                    assertThat(value).isNotNull();
                    assertThat(value.getContent()).isEqualTo("Updated content before delete");
                });

        // 3) Delete the post
        mockMvcTester
                .delete()
                .uri("/api/posts/" + title)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Wait for deletion to propagate (DB entry removed, redis key removed, local cache invalidated)
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsByTitle(title)).isFalse();
                    PostRedis value = postRedisRepository.findById(redisKey).orElse(null);
                    assertThat(value).isNull();
                    assertThat(localCache.getIfPresent(title)).isNull();
                });

        // 4) Subsequent GET should return 404
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> mockMvcTester
                        .get()
                        .uri("/api/posts/" + title)
                        .exchange()
                        .assertThat()
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .hasContentType(MediaType.APPLICATION_PROBLEM_JSON));

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(title)).isNull();
    }

    @Test
    void crudPostResourcesWithStateCheck() {
        String title = "delete-me";

        // 1) Create a post
        mockMvcTester
                .post()
                .uri("/api/posts")
                .content("""
          {
            "title": "delete-me",
            "content": "Will be deleted",
            "email": "test@local.com",
            "published": false,
            "publishedAt": null,
            "details": null,
            "tags": [
               {
                 "tagName": "java",
                 "tagDescription": "beautiful programming language"
               },
               {
                 "tagName": "spring",
                 "tagDescription": "the best framework"
               }
             ]
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
                    assertThat(postResponse.tags()).isNotEmpty().hasSize(2);
                    assertThat(postResponse.details()).isNull();
                });

        // Assert local cache and redis have the key
        String redisKey = "posts:" + title;
        String cached = localCache.getIfPresent(title);
        assertThat(cached).isNotNull();
        // as redis will take a short moment to be populated due to async nature, it should go through kafka and in
        // AggregatesToRedisListener value is set
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(
                        () -> assertThat(postRedisRepository.findById(redisKey)).isPresent());

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsByTitle(title)).isTrue();
                    assertThat(tagRepository.count()).isEqualTo(2);
                    assertThat(postTagRepository.countByPostEntity_Title(title)).isEqualTo(2);
                });

        // 2) Update the post via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/posts/" + title)
                .content("""
                        {
                          "title": "delete-me",
                          "content": "Updated content before delete",
                          "email": "test@local.com",
                          "published": true
                        }
                        """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse.content()).isEqualTo("Updated content before delete");
                    assertThat(postResponse.published()).isTrue();
                    assertThat(postResponse.publishedAt()).isNotNull();
                });

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsByTitle(title)).isTrue();
                    assertThat(tagRepository.count()).isEqualTo(2);
                    assertThat(postTagRepository.countByPostEntity_Title(title)).isEqualTo(2);
                });

        // Verify caches updated with new content
        String cachedAfter = localCache.getIfPresent(title);
        assertThat(cachedAfter).isNotNull();
        assertThat(cachedAfter).contains("Updated content before delete");
        // as redis will take a short moment to be populated due to async nature, it should go through kafka and in
        // AggregatesToRedisListener value is set
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PostRedis value = postRedisRepository.findById(redisKey).orElse(null);
                    assertThat(value).isNotNull();
                    assertThat(value.getContent()).isEqualTo("Updated content before delete");
                });

        // 3) Delete the post
        mockMvcTester
                .delete()
                .uri("/api/posts/" + title)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Wait for asynchronous tombstone processing to complete: DB row removed, post-tag relations cleared, and redis
        // key removed
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsByTitle(title)).isFalse();
                    assertThat(postTagRepository.countByPostEntity_Title(title)).isEqualTo(0);
                    assertThat(postRedisRepository.findById(redisKey)).isEmpty();
                });

        // Ensure tags themselves are still present (should be 2)
        assertThat(tagRepository.count()).isEqualTo(2);

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
        assertThat(postRedisRepository.findById(redisKey)).isEmpty();
    }
}
