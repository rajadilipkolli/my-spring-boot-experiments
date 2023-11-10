package com.example.graphql.querydsl.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.repositories.PostCommentRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class PostCommentControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PostCommentRepository postCommentRepository;

    private List<PostComment> postCommentList = null;

    @BeforeEach
    void setUp() {
        postCommentRepository.deleteAllInBatch();

        postCommentList = new ArrayList<>();
        postCommentList.add(new PostComment(null, "First PostComment"));
        postCommentList.add(new PostComment(null, "Second PostComment"));
        postCommentList.add(new PostComment(null, "Third PostComment"));
        postCommentList = postCommentRepository.saveAll(postCommentList);
    }

    @Test
    void shouldFetchAllPostComments() throws Exception {
        this.mockMvc
                .perform(get("/api/posts/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(postCommentList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindPostCommentById() throws Exception {
        PostComment postComment = postCommentList.get(0);
        Long postCommentId = postComment.getId();

        this.mockMvc
                .perform(get("/api/posts/comments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postComment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(postComment.getText())));
    }

    @Test
    void shouldCreateNewPostComment() throws Exception {
        PostCommentRequest postCommentRequest = new PostCommentRequest("New PostComment");
        this.mockMvc
                .perform(post("/api/posts/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(postCommentRequest.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostCommentWithoutText() throws Exception {
        PostCommentRequest postCommentRequest = new PostCommentRequest(null);

        this.mockMvc
                .perform(post("/api/posts/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts/comments")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePostComment() throws Exception {
        Long postCommentId = postCommentList.get(0).getId();
        PostCommentRequest postCommentRequest = new PostCommentRequest("Updated PostComment");

        this.mockMvc
                .perform(put("/api/posts/comments/{id}", postCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postCommentId), Long.class))
                .andExpect(jsonPath("$.text", is(postCommentRequest.text())));
    }

    @Test
    void shouldDeletePostComment() throws Exception {
        PostComment postComment = postCommentList.get(0);

        this.mockMvc
                .perform(delete("/api/posts/comments/{id}", postComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postComment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(postComment.getText())));
    }
}
