package com.example.learning.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    @Qualifier("jpaPostService") private PostService jpaPostService;

    @MockitoBean
    @Qualifier("jooqPostService") private PostService jooqPostService;

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
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/testuser/posts/"));

                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> violations = (List<Map<String, String>>)
                            problemDetail.getProperties().get("violations");
                    assertThat(violations)
                            .hasSize(5)
                            .satisfiesExactlyInAnyOrder(
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "comments[0].review")
                                                .containsEntry("rejectedValue", "")
                                                .containsEntry("message", "Review of post is mandatory");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "comments[0].title")
                                                .containsEntry("rejectedValue", "")
                                                .containsEntry("message", "Title of post comment is mandatory");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "content")
                                                .containsEntry("rejectedValue", "")
                                                .containsEntry("message", "Content of post can't be Blank");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "tags[0].name")
                                                .containsEntry("rejectedValue", "invalid@tag")
                                                .containsEntry(
                                                        "message",
                                                        "Tag name can only contain letters, numbers, hyphens and underscores");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "title")
                                                .containsEntry("rejectedValue", "")
                                                .containsEntry("message", "Title of post is mandatory");
                                    });
                });
    }

    @Test
    void createPost_shouldReturnValidationErrors() throws JsonProcessingException {
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
                    assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
                    assertThat(problemDetail.getTitle()).isEqualTo("Constraint Violation");
                    assertThat(problemDetail.getStatus()).isEqualTo(400);
                    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request content.");
                    assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/testuser/posts/"));

                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> violations = (List<Map<String, String>>)
                            problemDetail.getProperties().get("violations");
                    assertThat(violations)
                            .hasSize(6)
                            .satisfiesExactlyInAnyOrder(
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "comments[0].review")
                                                .containsEntry("message", "Review must not exceed 10000 characters");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "comments[0].title")
                                                .containsEntry("message", "Title must not exceed 255 characters");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "content")
                                                .containsEntry("message", "Content must not exceed 10000 characters");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "tags[0].name")
                                                .containsEntry(
                                                        "message", "Tag name must be between 1 and 50 characters");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "tags[0].description")
                                                .containsEntry(
                                                        "message", "Tag description cannot exceed 200 characters");
                                    },
                                    violation -> {
                                        assertThat(violation)
                                                .containsEntry("object", "postRequest")
                                                .containsEntry("field", "title")
                                                .containsEntry("message", "Title must not exceed 255 characters");
                                    });
                });
    }
}
