package com.example.highrps.postcomment.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.entities.AuthorEntity;
import com.example.highrps.entities.PostCommentEntity;
import com.example.highrps.entities.PostDetailsEntity;
import com.example.highrps.entities.PostEntity;
import com.example.highrps.shared.IdGenerator;
import java.time.Duration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PostCommentControllerIT extends AbstractIntegrationTest {

    private Long postId;

    @BeforeEach
    void setUp() {
        // Clean up repositories before each test
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        authorRepository.deleteAll();
        // Create an author
        AuthorEntity author = new AuthorEntity()
                .setEmail("comment-test@example.com")
                .setFirstName("Test")
                .setLastName("User")
                .setMobile(1234567890L);
        PostDetailsEntity postDetailsEntity = new PostDetailsEntity();
        postDetailsEntity.setCreatedBy("Test");
        PostEntity postEntity = new PostEntity("Test Post for Comments", "Post content", author);
        postEntity.setPostRefId(IdGenerator.generateLong());
        postEntity.setDetails(postDetailsEntity);
        author.addPost(postEntity);
        AuthorEntity authorEntity = authorRepository.save(author);
        postId = authorEntity.getPostEntities().getFirst().getPostRefId();
    }

    @Test
    void shouldCreatePostComment() {
        long count = postCommentRepository.count();
        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                {
                                                  "title": "Great post!",
                                                  "content": "This is a very insightful comment.",
                                                  "published": true
                                                }
                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .containsHeader("Location")
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> {
                    assertThat(response.commentId()).isNotNull();
                    assertThat(response.title()).isEqualTo("Great post!");
                    assertThat(response.content()).isEqualTo("This is a very insightful comment.");
                    assertThat(response.published()).isTrue();
                    assertThat(response.publishedAt()).isNotNull();
                    assertThat(response.postId()).isEqualTo(postId);
                    assertThat(response.createdAt()).isNotNull();
                    assertThat(response.modifiedAt()).isNull();
                });

        await().atMost(Duration.ofSeconds(60))
                .pollDelay(Duration.ofSeconds(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(postCommentRepository.count()).isEqualTo(count + 1));
    }

    @Test
    void shouldGetPostCommentById() {
        // Create a comment first
        Long[] commentIdHolder = new Long[1];
        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                                {
                                                                  "title": "Test Comment",
                                                                  "content": "Test content",
                                                                  "published": false
                                                                }
                                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> commentIdHolder[0] = response.commentId());

        Long commentId = commentIdHolder[0];

        // Get the comment by ID
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}/comments/{postCommentId}", postId, commentId)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> {
                    assertThat(response.commentId()).isEqualTo(commentId);
                    assertThat(response.title()).isEqualTo("Test Comment");
                    assertThat(response.content()).isEqualTo("Test content");
                    assertThat(response.published()).isFalse();
                    assertThat(response.publishedAt()).isNull();
                    assertThat(response.postId()).isEqualTo(postId);
                    assertThat(response.createdAt()).isNotNull();
                    assertThat(response.modifiedAt()).isNull();
                });

        await().atMost(Duration.ofSeconds(60))
                .pollDelay(Duration.ofSeconds(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(postCommentRepository.findByCommentRefIdAndPostRefId(commentId, postId))
                        .isPresent());
    }

    @Test
    void shouldGetAllCommentsForPost() {
        // Create multiple comments
        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                {
                                                  "title": "First Comment",
                                                  "content": "First content",
                                                  "published": true
                                                }
                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                {
                                                  "title": "Second Comment",
                                                  "content": "Second content",
                                                  "published": false
                                                }
                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        // Get all comments
        await().atMost(Duration.ofSeconds(60))
                .pollDelay(Duration.ofSeconds(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> mockMvcTester
                        .get()
                        .uri("/api/posts/{postId}/comments", postId)
                        .exchange()
                        .assertThat()
                        .hasStatus(HttpStatus.OK)
                        .hasContentType(MediaType.APPLICATION_JSON)
                        .bodyJson()
                        .convertTo(InstanceOfAssertFactories.list(PostCommentResponse.class))
                        .hasSize(2));
    }

    @Test
    void shouldUpdatePostComment() {
        // Create a comment
        Long[] commentIdHolder = new Long[1];
        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                                {
                                                                  "title": "Original Title",
                                                                  "content": "Original content",
                                                                  "published": false
                                                                }
                                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> commentIdHolder[0] = response.commentId());

        Long commentId = commentIdHolder[0];

        // Update the comment
        mockMvcTester
                .put()
                .uri("/api/posts/{postId}/comments/{postCommentId}", postId, commentId)
                .content("""
                                                {
                                                  "title": "Updated Title",
                                                  "content": "Updated content",
                                                  "published": true
                                                }
                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> {
                    assertThat(response.commentId()).isEqualTo(commentId);
                    assertThat(response.title()).isEqualTo("Updated Title");
                    assertThat(response.content()).isEqualTo("Updated content");
                    assertThat(response.published()).isTrue();
                    assertThat(response.publishedAt()).isNotNull();
                    assertThat(response.modifiedAt()).isNotNull();
                    assertThat(response.postId()).isEqualTo(postId);
                });

        await().atMost(Duration.ofSeconds(60))
                .pollDelay(Duration.ofSeconds(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(postCommentRepository.findByCommentRefIdAndPostRefId(commentId, postId))
                        .isPresent()
                        .satisfies(postCommentEntity -> {
                            PostCommentEntity commentEntity = postCommentEntity.get();
                            assertThat(commentEntity.getTitle()).isEqualTo("Updated Title");
                            assertThat(commentEntity.getContent()).isEqualTo("Updated content");
                            assertThat(commentEntity.isPublished()).isTrue();
                            assertThat(commentEntity.getPublishedAt()).isNotNull();
                            assertThat(commentEntity.getCreatedAt()).isNotNull();
                            assertThat(commentEntity.getModifiedAt()).isNotNull();
                        }));
    }

    @Test
    void shouldDeletePostComment() {
        // Create a comment
        Long[] commentIdHolder = new Long[1];
        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                                {
                                                                  "title": "To be deleted",
                                                                  "content": "This will be deleted",
                                                                  "published": false
                                                                }
                                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> commentIdHolder[0] = response.commentId());

        Long commentId = commentIdHolder[0];

        // Delete the comment
        mockMvcTester
                .delete()
                .uri("/api/posts/{postId}/comments/{postCommentId}", postId, commentId)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}/comments/{postCommentId}", postId, commentId)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);

        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(postCommentRepository.findByCommentRefIdAndPostRefId(commentId, postId))
                        .isEmpty());
    }

    @Test
    void shouldReturn404WhenCommentNotFound() {
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}/comments/{postCommentId}", postId, 99999L)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void shouldReturn404WhenCommentDoesNotBelongToPost() {
        // Create a comment for this post
        Long[] commentIdHolder = new Long[1];
        mockMvcTester
                .post()
                .uri("/api/posts/{postId}/comments", postId)
                .content("""
                                                                {
                                                                  "title": "Comment",
                                                                  "content": "Content",
                                                                  "published": false
                                                                }
                                                                """)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(PostCommentResponse.class)
                .satisfies(response -> commentIdHolder[0] = response.commentId());

        Long commentId = commentIdHolder[0];

        // Try to access it with a different postId
        mockMvcTester
                .get()
                .uri("/api/posts/{postId}/comments/{postCommentId}", 99999L, commentId)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
