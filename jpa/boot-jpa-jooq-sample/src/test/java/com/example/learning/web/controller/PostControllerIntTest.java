package com.example.learning.web.controller;

import com.example.learning.common.AbstractIntegrationTest;
import com.example.learning.entities.Tag;
import com.example.learning.model.request.PostCommentRequest;
import com.example.learning.model.request.PostRequest;
import com.example.learning.model.request.TagRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PostControllerIntTest extends AbstractIntegrationTest {

    @Test
    void createPostByUserName() throws JsonProcessingException {

        Tag tag = new Tag().setTagName("spring").setTagDescription("Beautiful Spring");
        tagRepository.save(tag);

        PostRequest postRequest = new PostRequest(
                "newPostTitle",
                "newPostContent",
                true,
                LocalDateTime.now(),
                List.of(
                        new PostCommentRequest(
                                "commentTitle1",
                                "Nice Post1",
                                true,
                                LocalDateTime.now().plusDays(1)),
                        new PostCommentRequest(
                                "commentTitle2",
                                "Nice Post2",
                                true,
                                LocalDateTime.now().minusDays(1))),
                List.of(
                        new TagRequest("junit", "Junit Tag"),
                        new TagRequest("spring", "Beautiful Spring"),
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
    }
}
