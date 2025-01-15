package com.example.learning.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.model.response.PostResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

class PostControllerIntTest extends AbstractIntegrationTest {

    @Test
    void createPostByUserName() throws JsonProcessingException {

        Tag tagEntity = new Tag().setTagName("spring").setTagDescription("Beautiful Spring");
        tagRepository.save(tagEntity);

        PostRequest postRequest = new PostRequest(
                "newPostTitle",
                "newPostContent",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1", "Nice Post1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "commentTitle2", "Nice Post2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of(
                        new TagRequest("junit", "Junit Tag"),
                        new TagRequest("spring", "new Description Spring"),
                        new TagRequest("Java", "Beautiful Java")));

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/newPostTitle");

        // Verify that existing tags were reused
        assertThat(tagRepository.findByTagName("spring")).isPresent().get().satisfies(retrieveTag -> {
            assertThat(retrieveTag.getTagName()).isEqualTo("spring");
            assertThat(retrieveTag.getTagDescription()).isEqualTo("Beautiful Spring"); // Original description retained
        });

        this.mockMvcTester
                .get()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "newPostTitle")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("newPostTitle");
                    assertThat(postResponse.content()).isEqualTo("newPostContent");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-15T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.createdAt()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
                    assertThat(postResponse.tags())
                            .isNotNull()
                            .hasSize(3)
                            .satisfiesExactlyInAnyOrder(
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("junit");
                                        assertThat(tag.description()).isEqualTo("Junit Tag");
                                    },
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("spring");
                                        assertThat(tag.description()).isEqualTo("Beautiful Spring");
                                    },
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("Java");
                                        assertThat(tag.description()).isEqualTo("Beautiful Java");
                                    });
                    assertThat(postResponse.comments())
                            .isNotNull()
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("commentTitle1");
                                        assertThat(comment.content()).isEqualTo("Nice Post1");
                                        assertThat(comment.published()).isTrue();
                                    },
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("commentTitle2");
                                        assertThat(comment.content()).isEqualTo("Nice Post2");
                                        assertThat(comment.published()).isTrue();
                                    });
                });
    }

    @Test
    void createPostWithoutTagsAndComments() throws JsonProcessingException {
        PostRequest postRequest = new PostRequest(
                "Simple Post",
                "This is a simple post without tags and comments",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(), // empty comments list
                List.of()); // empty tags list

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/Simple%20Post");

        this.mockMvcTester
                .get()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "Simple Post")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("Simple Post");
                    assertThat(postResponse.content()).isEqualTo("This is a simple post without tags and comments");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-15T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.createdAt()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
                    assertThat(postResponse.tags()).isNotNull().isEmpty();
                    assertThat(postResponse.comments()).isNotNull().isEmpty();
                });
    }

    @Test
    void shouldFailForInvalidInput() throws Exception {
        // Test with blank title
        PostRequest invalidRequest = new PostRequest(
                "", // blank title
                "content",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(),
                List.of());

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/junit/posts/"));
                });

        // Test with blank content
        invalidRequest = new PostRequest(
                "title",
                "", // blank content
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(),
                List.of());

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/junit/posts/"));
                });
    }

    @Test
    void createPostByUserName_shouldReturnValidationErrors() throws Exception {
        // Create a post with all validation errors
        PostRequest invalidPost = new PostRequest(
                "", // blank title
                "", // blank content
                true,
                LocalDateTime.now(),
                List.of(new PostCommentRequest("", "", true, LocalDateTime.now())), // invalid comment
                List.of(new TagRequest("invalid@tag", "desc")) // invalid tag name pattern
                );

        // Test blank fields
        mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPost))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/testuser/posts/"));
                });

        // Create a post with max length violations
        String exceededTitle = "a".repeat(256);
        String exceededContent = "a".repeat(10001);
        PostRequest postWithExceededLengths = new PostRequest(
                exceededTitle,
                exceededContent,
                true,
                LocalDateTime.now(),
                List.of(new PostCommentRequest("a".repeat(256), "a".repeat(10001), true, LocalDateTime.now())),
                List.of(new TagRequest("a".repeat(51), "a".repeat(201))));

        // Test max length violations
        mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postWithExceededLengths))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/testuser/posts/"));
                });
    }

    @Test
    void shouldHandleDuplicateTitle() throws Exception {
        // Create first post
        PostRequest firstPost = new PostRequest(
                "Duplicate Title",
                "First Content",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(),
                List.of());

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstPost))
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        // Try to create second post with same title
        PostRequest duplicatePost = new PostRequest(
                "Duplicate Title",
                "Second Content",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(),
                List.of());

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicatePost))
                .assertThat()
                .hasStatus(HttpStatus.CONFLICT) // HTTP 409 Conflict
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> {
                    assertThat(problemDetail.getTitle()).isEqualTo("Duplicate entry");
                    assertThat(problemDetail.getStatus()).isEqualTo(409);
                    assertThat(problemDetail.getDetail()).isEqualTo("Post with title : Duplicate Title already exists");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/junit/posts/"));
                    assertThat(problemDetail.getProperties())
                            .isNotNull()
                            .isNotEmpty()
                            .hasSize(2);
                });
    }
}
