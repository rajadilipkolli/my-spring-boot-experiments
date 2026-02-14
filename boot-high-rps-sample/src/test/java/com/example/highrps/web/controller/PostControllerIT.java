package com.example.highrps.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.entities.AuthorEntity;
import com.example.highrps.entities.PostRedis;
import com.example.highrps.model.response.PostDetailsResponse;
import com.example.highrps.model.response.PostResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
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
                          "email": "junit@email.com",
                          "details": {
                            "detailsKey": "This is a summary",
                            "createdBy": "JunitIteration"
                          }
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
    void getPostByPostIdNotFound() {
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}", 999999L)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void crudPostResourcesAPICheck() {
        String title = "sample-post";
        String email = "test1@local.com";

        authorRepository.save(new AuthorEntity()
                .setEmail(email)
                .setFirstName("FirstName")
                .setLastName("LastName")
                .setMobile(9876543210L));

        // 1) Create a post
        AtomicReference<Long> postId = new AtomicReference<>();
        mockMvcTester
                .post()
                .uri("/api/posts")
                .content("""
          {
            "title": "sample-post",
            "content": "Will be deleted later",
            "email": "test1@local.com",
            "details": {
                "detailsKey": "This is a summary",
                "createdBy": "JunitIteration"
            }
          }
          """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .apply(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).isNotNull();
                    assertThat(location).contains("/api/posts/");
                    postId.set(Long.valueOf(location.substring(location.lastIndexOf("/") + 1)));
                });

        // Ensure caches/redis are populated by hitting GET (which populates local cache and redis)
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}", postId.get())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse.postId()).isEqualTo(postId.get());
                    assertThat(postResponse.title()).isEqualTo(title);
                    assertThat(postResponse.content()).isEqualTo("Will be deleted later");
                    assertThat(postResponse.published()).isFalse();
                    assertThat(postResponse.publishedAt()).isNull();
                    assertThat(postResponse.createdAt()).isNotNull().isInstanceOf(LocalDateTime.class);
                    assertThat(postResponse.modifiedAt()).isNull();
                    assertThat(postResponse.tags()).isNull();
                    assertThat(postResponse.details()).isNotNull();
                    assertThat(postResponse.details().detailsKey()).isEqualTo("This is a summary");
                    assertThat(postResponse.details().createdBy()).isEqualTo("JunitIteration");
                });

        // Assert local cache and redis have the key
        String cacheKey = String.valueOf(postId.get());
        String cached = localCache.getIfPresent(cacheKey);
        assertThat(cached).isNotNull();
        // Redis may be populated synchronously or asynchronously depending on timing and listener implementation.
        // Make the test robust by awaiting eventual consistency.
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PostRedis value = postRedisRepository.findById(postId.get()).orElse(null);
                    assertThat(value).isNotNull();
                    assertThat(value.getContent()).isEqualTo("Will be deleted later");
                    assertThat(value.isPublished()).isFalse();
                    assertThat(value.getPublishedAt()).isNull();
                    assertThat(value.getCreatedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
                    assertThat(value.getModifiedAt()).isNull();
                });

        // 2) Update the post via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/posts/" + postId.get())
                .content("""
                        {
                            "postId": %d,
                            "title": "sample-post",
                            "content": "Updated content before delete",
                            "email": "test1@local.com",
                            "published": true,
                            "details": {
                                "detailsKey": "This is a summary",
                                "createdBy": "Some additional info"
                            }
                        }
                        """.formatted(postId.get()))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(resp -> {
                    assertThat(resp.content()).isEqualTo("Updated content before delete");
                    assertThat(resp.modifiedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
                });

        // Verify caches updated with new content
        String cachedAfter = localCache.getIfPresent(cacheKey);
        assertThat(cachedAfter).isNotNull();
        assertThat(cachedAfter).contains("Updated content before delete");
        // Redis may be updated asynchronously via Kafka; await the updated aggregate.
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PostRedis value = postRedisRepository.findById(postId.get()).orElse(null);
                    assertThat(value).isNotNull();
                    assertThat(value.getContent()).isEqualTo("Updated content before delete");
                    assertThat(value.isPublished()).isTrue();
                    assertThat(value.getPublishedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
                    assertThat(value.getCreatedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
                    assertThat(value.getModifiedAt()).isNotNull().isInstanceOf(LocalDateTime.class);
                    assertThat(value.getModifiedAt()).isAfter(value.getCreatedAt());
                });

        // 3) Delete the post
        mockMvcTester
                .delete()
                .uri("/api/posts/{postId}", postId.get())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Wait for deletion to propagate (DB entry removed, redis key removed, local cache invalidated)
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsById(postId.get())).isFalse();
                    assertThat(postRedisRepository.existsById(postId.get())).isFalse();
                    assertThat(localCache.getIfPresent(cacheKey)).isNull();
                });

        // 4) Subsequent GET should return 404
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> mockMvcTester
                        .get()
                        .uri("/api/posts/{postId}", postId.get())
                        .exchange()
                        .assertThat()
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .hasContentType(MediaType.APPLICATION_PROBLEM_JSON));

        // Also assert local cache and redis no longer have the key
        assertThat(localCache.getIfPresent(cacheKey)).isNull();
    }

    @Test
    void crudPostResourcesWithStateCheck() {
        String title = "delete-me";
        String email = "test@local.com";

        AuthorEntity entity = new AuthorEntity()
                .setEmail(email)
                .setFirstName("FirstName")
                .setLastName("LastName")
                .setMobile(9876543210L);
        entity.setCreatedAt(LocalDateTime.now());
        authorRepository.save(entity);

        // 1) Create a post
        AtomicReference<Long> postId = new AtomicReference<>();
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
            "details": {
                "detailsKey": "This is a summary",
                "createdBy": "JunitIteration"
            },
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
                .hasStatus(HttpStatus.CREATED)
                .apply(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assertThat(location).isNotNull();
                    assertThat(location).contains("/api/posts/");
                    postId.set(Long.valueOf(location.substring(location.lastIndexOf("/") + 1)));
                });

        // Ensure caches/redis are populated by hitting GET (which populates local cache and redis)
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}", postId.get())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse.postId()).isEqualTo(postId.get());
                    assertThat(postResponse.title()).isEqualTo(title);
                    assertThat(postResponse.content()).isEqualTo("Will be deleted");
                    assertThat(postResponse.published()).isFalse();
                    assertThat(postResponse.publishedAt()).isNull();
                    assertThat(postResponse.tags()).isNotEmpty().hasSize(2);
                    PostDetailsResponse details = postResponse.details();
                    assertThat(details).isNotNull();
                    assertThat(details.detailsKey()).isEqualTo("This is a summary");
                    assertThat(details.createdBy()).isEqualTo("JunitIteration");
                });

        // Assert local cache and redis have the cacheKey
        String cacheKey = String.valueOf(postId.get());
        String cached = localCache.getIfPresent(cacheKey);
        assertThat(cached).isNotNull();
        // as redis will take a short moment to be populated due to async nature, it should go through kafka and in
        // AggregatesToRedisListener value is set
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        assertThat(postRedisRepository.findById(postId.get())).isPresent());

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsByPostRefId(postId.get())).isTrue();
                    assertThat(tagRepository.count()).isEqualTo(2);
                    assertThat(postTagRepository.countByPostEntity_Title(title)).isEqualTo(2);
                });

        // 2) Update the post via the new PUT endpoint to change content
        mockMvcTester
                .put()
                .uri("/api/posts/{postId}", postId.get())
                .content("""
                        {
                          "postId": %d,
                          "title": "delete-me",
                          "content": "Updated content before delete",
                          "email": "test@local.com",
                          "published": true,
                          "details": {
                            "detailsKey": "This is a summary",
                            "createdBy": "JunitIteration"
                          }
                        }
                        """.formatted(postId.get()))
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
                    assertThat(postRepository.existsByPostRefId(postId.get())).isTrue();
                    assertThat(tagRepository.count()).isEqualTo(2);
                    assertThat(postTagRepository.countByPostEntity_Title(title)).isEqualTo(2);
                });

        // Verify caches updated with new content
        String cachedAfter = localCache.getIfPresent(cacheKey);
        assertThat(cachedAfter).isNotNull();
        assertThat(cachedAfter).contains("Updated content before delete");
        // as redis will take a short moment to be populated due to async nature, it should go through kafka and in
        // AggregatesToRedisListener value is set
        await().atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    PostRedis value = postRedisRepository.findById(postId.get()).orElse(null);
                    assertThat(value).isNotNull();
                    assertThat(value.getContent()).isEqualTo("Updated content before delete");
                });

        // 3) Delete the post
        mockMvcTester
                .delete()
                .uri("/api/posts/{postId}", postId.get())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Wait for asynchronous tombstone processing to complete: DB row removed, post-tag relations cleared, and redis
        // cacheKey removed
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    assertThat(postRepository.existsByPostRefId(postId.get())).isFalse();
                    assertThat(postTagRepository.countByPostEntity_Title(title)).isEqualTo(0);
                });

        // Ensure tags themselves are still present (should be 2)
        assertThat(tagRepository.count()).isEqualTo(2);

        // 4) Subsequent GET should return 404
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}", postId.get())
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

        // Also assert local cache and redis no longer have the cacheKey
        assertThat(localCache.getIfPresent(cacheKey)).isNull();
        assertThat(postRedisRepository.existsById(postId.get())).isFalse();
    }
}
