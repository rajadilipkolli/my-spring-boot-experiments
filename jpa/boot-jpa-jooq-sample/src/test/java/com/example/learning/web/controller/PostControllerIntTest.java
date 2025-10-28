package com.example.learning.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.example.learning.model.response.PostCommentResponse;
import com.example.learning.model.response.PostResponse;
import com.example.learning.model.response.TagResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
                .content(jsonMapper.writeValueAsString(postRequest))
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
                .content(jsonMapper.writeValueAsString(invalidRequest))
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
                .content(jsonMapper.writeValueAsString(invalidRequest))
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
                .content(jsonMapper.writeValueAsString(firstPost))
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
                .content(jsonMapper.writeValueAsString(duplicatePost))
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

    @Test
    void createPostWithCommentsWithoutTagsAndDelete() throws JsonProcessingException {
        PostRequest postRequest = new PostRequest(
                "postWithCommentsWithOutTags",
                "This is a simple post without tags",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1", "Nice Post1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "commentTitle2", "Nice Post2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of()); // empty tags list

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/postWithCommentsWithOutTags");

        this.mockMvcTester
                .delete()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "postWithCommentsWithOutTags")
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldThrowExceptionWhenPostNotFoundWhileRetrieving() {
        this.mockMvcTester
                .get()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "NonExistingPost")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> assertNotFoundProblemDetail(problemDetail, "NonExistingPost"));
    }

    private void assertNotFoundProblemDetail(ProblemDetail problemDetail, String title) {
        assertThat(problemDetail.getType()).isEqualTo(URI.create("https://api.boot-jpa-jooq.com/errors/not-found"));
        assertThat(problemDetail.getTitle()).isEqualTo("Not Found");
        assertThat(problemDetail.getStatus()).isEqualTo(404);
        assertThat(problemDetail.getDetail()).isEqualTo("Post with title '" + title + "' not found for user 'junit'");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/api/users/junit/posts/" + title));
    }

    @Test
    void shouldThrowExceptionWhenPostNotFoundWhileDeleting() {
        this.mockMvcTester
                .delete()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "NonExistingPost")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(problemDetail -> assertNotFoundProblemDetail(problemDetail, "NonExistingPost"));
    }

    @Test
    void updatePostByUserNameWithNewCommentsAndTags() throws JsonProcessingException {

        PostRequest postRequest = new PostRequest(
                "titleWithNewCommentsAndTags",
                "This is a simple post without tags",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1", "Nice Post1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "commentTitle2", "Nice Post2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of()); // empty tags list

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/titleWithNewCommentsAndTags");

        postRequest = new PostRequest(
                "existingPostTitle",
                "updatedContent",
                true,
                LocalDateTime.parse("2025-01-16T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "newCommentTitle1", "New Comment1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest("commentTitle2", "Nice Post2", false, null),
                        new PostCommentRequest(
                                "newCommentTitle2", "New Comment2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of(new TagRequest("newTag1", "New Tag 1"), new TagRequest("newTag2", "New Tag 2")));

        this.mockMvcTester
                .put()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "titleWithNewCommentsAndTags")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("existingPostTitle");
                    assertThat(postResponse.content()).isEqualTo("updatedContent");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-16T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.tags())
                            .isNotNull()
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("newTag1");
                                        assertThat(tag.description()).isEqualTo("New Tag 1");
                                    },
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("newTag2");
                                        assertThat(tag.description()).isEqualTo("New Tag 2");
                                    });
                    assertThat(postResponse.comments())
                            .isNotNull()
                            .hasSize(3)
                            .satisfiesExactlyInAnyOrder(
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("newCommentTitle1");
                                        assertThat(comment.content()).isEqualTo("New Comment1");
                                        assertThat(comment.published()).isTrue();
                                        assertThat(comment.publishedAt()).isNotNull();
                                    },
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("newCommentTitle2");
                                        assertThat(comment.content()).isEqualTo("New Comment2");
                                        assertThat(comment.published()).isTrue();
                                        assertThat(comment.publishedAt()).isNotNull();
                                    },
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("commentTitle2");
                                        assertThat(comment.content()).isEqualTo("Nice Post2");
                                        assertThat(comment.published()).isFalse();
                                        assertThat(comment.publishedAt()).isNull();
                                    });
                });
    }

    @Test
    void updatePostByUserNameWithRemovedCommentsAndTags() throws JsonProcessingException {

        PostRequest postRequest = new PostRequest(
                "postWithOutTags",
                "This is a simple post without tags",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1", "Nice Post1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "commentTitle2", "Nice Post2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of()); // empty tags list

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/postWithOutTags");

        postRequest = new PostRequest(
                "postWithOutTags",
                "updatedContent",
                true,
                LocalDateTime.parse("2025-01-16T10:00:00"),
                List.of(), // empty comments list
                List.of()); // empty tags list

        this.mockMvcTester
                .put()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "postWithOutTags")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("postWithOutTags");
                    assertThat(postResponse.content()).isEqualTo("updatedContent");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-16T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.tags()).isNotNull().isEmpty();
                    assertThat(postResponse.comments()).isNotNull().isEmpty();
                });
    }

    @Test
    void updatePostByUserNameWithExistingCommentsAndTags() throws JsonProcessingException {
        PostRequest postRequest = new PostRequest(
                "existingPostTitleWithTagsAndComments",
                "updatedContent",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest("existingCommentTitle1", "Updated Comment1", false, null),
                        new PostCommentRequest("existingCommentTitle2", "Updated Comment2", false, null)),
                List.of(new TagRequest("existingTag1", "Updated Tag"), new TagRequest("existingTag2", "Updated Tag")));

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(
                        HttpHeaders.LOCATION,
                        "http://localhost/api/users/junit/posts/existingPostTitleWithTagsAndComments");

        postRequest = new PostRequest(
                "existingPostTitle",
                "updatedContent",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "existingCommentTitle1",
                                "Updated Comment1",
                                true,
                                LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "existingCommentTitle2",
                                "Updated Comment2",
                                true,
                                LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of(
                        new TagRequest("existingTag1", "Updated Tag 1"),
                        new TagRequest("existingTag2", "Updated Tag 2")));

        this.mockMvcTester
                .put()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "existingPostTitleWithTagsAndComments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("existingPostTitle");
                    assertThat(postResponse.content()).isEqualTo("updatedContent");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-15T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.tags())
                            .isNotNull()
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("existingTag1");
                                        assertThat(tag.description()).isEqualTo("Updated Tag 1");
                                    },
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("existingTag2");
                                        assertThat(tag.description()).isEqualTo("Updated Tag 2");
                                    });
                    assertThat(postResponse.comments())
                            .isNotNull()
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("existingCommentTitle1");
                                        assertThat(comment.content()).isEqualTo("Updated Comment1");
                                        assertThat(comment.published()).isTrue();
                                    },
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("existingCommentTitle2");
                                        assertThat(comment.content()).isEqualTo("Updated Comment2");
                                        assertThat(comment.published()).isTrue();
                                    });
                });
    }

    @Test
    void updatePostByUserNameWithNoCommentsAndTags() throws JsonProcessingException {
        PostRequest postRequest = new PostRequest(
                "existingPostTitle",
                "This is a simple post without tags",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1", "Nice Post1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "commentTitle2", "Nice Post2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of()); // empty tags list

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/existingPostTitle");

        postRequest = new PostRequest(
                "existingPostTitle",
                "updatedContent",
                true,
                LocalDateTime.parse("2025-01-16T10:00:00"),
                List.of(), // empty comments list
                List.of()); // empty tags list

        this.mockMvcTester
                .put()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "existingPostTitle")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("existingPostTitle");
                    assertThat(postResponse.content()).isEqualTo("updatedContent");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-16T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.tags()).isNotNull().isEmpty();
                    assertThat(postResponse.comments()).isNotNull().isEmpty();
                });
    }

    @Test
    void updatePostByUserNameWithDuplicateTags() throws JsonProcessingException {
        PostRequest postRequest = new PostRequest(
                "titleWithDuplicateTags",
                "This is a simple post with tags",
                true,
                LocalDateTime.parse("2025-01-15T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1", "Nice Post1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest(
                                "commentTitle2", "Nice Post2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of(new TagRequest("tag1", "Tag 1"), new TagRequest("tag2", "Tag 2")));

        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/titleWithDuplicateTags");

        postRequest = new PostRequest(
                "existingPostTitle",
                "updatedContent",
                true,
                LocalDateTime.parse("2025-01-16T10:00:00"),
                List.of(
                        new PostCommentRequest(
                                "newCommentTitle1", "New Comment1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                        new PostCommentRequest("commentTitle2", "Nice Post2", false, null),
                        new PostCommentRequest(
                                "newCommentTitle2", "New Comment2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                List.of(new TagRequest("tag1", "Tag 1"), new TagRequest("tag2", "Tag 2")));

        this.mockMvcTester
                .put()
                .uri("/api/users/{user_name}/posts/{title}", "junit", "titleWithDuplicateTags")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo("existingPostTitle");
                    assertThat(postResponse.content()).isEqualTo("updatedContent");
                    assertThat(postResponse.published()).isNotNull().isEqualTo(true);
                    assertThat(postResponse.publishedAt())
                            .isNotNull()
                            .isEqualTo(LocalDateTime.parse("2025-01-16T10:00:00"));
                    assertThat(postResponse.author()).isNotNull().isEqualTo("junit");
                    assertThat(postResponse.tags())
                            .isNotNull()
                            .hasSize(2)
                            .satisfiesExactlyInAnyOrder(
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("tag1");
                                        assertThat(tag.description()).isEqualTo("Tag 1");
                                    },
                                    tag -> {
                                        assertThat(tag.name()).isEqualTo("tag2");
                                        assertThat(tag.description()).isEqualTo("Tag 2");
                                    });
                    assertThat(postResponse.comments())
                            .isNotNull()
                            .hasSize(3)
                            .satisfiesExactlyInAnyOrder(
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("newCommentTitle1");
                                        assertThat(comment.content()).isEqualTo("New Comment1");
                                        assertThat(comment.published()).isTrue();
                                        assertThat(comment.publishedAt()).isNotNull();
                                    },
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("newCommentTitle2");
                                        assertThat(comment.content()).isEqualTo("New Comment2");
                                        assertThat(comment.published()).isTrue();
                                        assertThat(comment.publishedAt()).isNotNull();
                                    },
                                    comment -> {
                                        assertThat(comment.title()).isEqualTo("commentTitle2");
                                        assertThat(comment.content()).isEqualTo("Nice Post2");
                                        assertThat(comment.published()).isFalse();
                                        assertThat(comment.publishedAt()).isNull();
                                    });
                });
    }

    @ParameterizedTest
    @MethodSource("postTestCases")
    void shouldHandlePostOperationsWithDifferentConfigurations(
            String title, List<PostCommentRequest> comments, List<TagRequest> tags, boolean expectEmpty)
            throws JsonProcessingException {

        PostRequest postRequest = new PostRequest(
                title, "Test content", true, LocalDateTime.parse("2025-01-15T10:00:00"), comments, tags);

        // Test creation
        this.mockMvcTester
                .post()
                .uri("/api/users/{user_name}/posts/", "junit")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(postRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/users/junit/posts/" + title);

        // Test retrieval
        this.mockMvcTester
                .get()
                .uri("/api/users/{user_name}/posts/{title}", "junit", title)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(PostResponse.class)
                .satisfies(postResponse -> {
                    assertThat(postResponse).isNotNull();
                    assertThat(postResponse.title()).isEqualTo(title);
                    assertThat(postResponse.content()).isEqualTo("Test content");
                    assertThat(postResponse.published()).isTrue();
                    assertThat(postResponse.publishedAt()).isEqualTo(LocalDateTime.parse("2025-01-15T10:00:00"));
                    assertThat(postResponse.author()).isEqualTo("junit");
                    assertThat(postResponse.createdAt()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());

                    if (expectEmpty) {
                        assertThat(postResponse.comments()).isEmpty();
                        assertThat(postResponse.tags()).isEmpty();
                    } else {
                        if (!comments.isEmpty()) {
                            @SuppressWarnings("unchecked")
                            Consumer<PostCommentResponse>[] commentConsumers = comments.stream()
                                    .<Consumer<PostCommentResponse>>map(expected -> actual -> {
                                        assertThat(actual.title()).isEqualTo(expected.title());
                                        assertThat(actual.content()).isEqualTo(expected.review());
                                        assertThat(actual.published()).isEqualTo(expected.published());
                                        assertThat(actual.publishedAt()).isNotNull();
                                    })
                                    .toArray(Consumer[]::new);
                            assertThat(postResponse.comments())
                                    .hasSize(comments.size())
                                    .satisfiesExactlyInAnyOrder(commentConsumers);
                        }
                        if (!tags.isEmpty()) {
                            @SuppressWarnings("unchecked")
                            Consumer<TagResponse>[] tagConsumers = tags.stream()
                                    .<Consumer<TagResponse>>map(expected -> actual -> {
                                        assertThat(actual.name()).isEqualTo(expected.name());
                                        assertThat(actual.description()).isEqualTo(expected.description());
                                    })
                                    .toArray(Consumer[]::new);
                            assertThat(postResponse.tags())
                                    .hasSize(tags.size())
                                    .satisfiesExactlyInAnyOrder(tagConsumers);
                        }
                    }
                });
    }

    static Stream<Arguments> postTestCases() {
        return Stream.of(
                // Empty post (no comments, no tags)
                Arguments.of("emptyPost", List.of(), List.of(), true),

                // Post with comments only
                Arguments.of(
                        "postWithComments",
                        List.of(
                                new PostCommentRequest(
                                        "comment1", "content1", true, LocalDateTime.parse("2025-01-16T10:00:00")),
                                new PostCommentRequest(
                                        "comment2", "content2", true, LocalDateTime.parse("2025-01-14T10:00:00"))),
                        List.of(),
                        false),

                // Post with tags only
                Arguments.of(
                        "postWithTags",
                        List.of(),
                        List.of(new TagRequest("tag1", "Tag 1"), new TagRequest("tag2", "Tag 2")),
                        false),

                // Post with both comments and tags
                Arguments.of(
                        "postWithCommentsAndTags",
                        List.of(new PostCommentRequest(
                                "comment1", "content1", true, LocalDateTime.parse("2025-01-16T10:00:00"))),
                        List.of(new TagRequest("tag1", "Tag 1")),
                        false));
    }
}
