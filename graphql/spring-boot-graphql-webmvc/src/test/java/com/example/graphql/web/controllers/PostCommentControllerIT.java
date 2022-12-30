package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostComment;
import com.example.graphql.repositories.PostCommentRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PostCommentControllerIT extends AbstractIntegrationTest {

    @Autowired private PostCommentRepository postCommentRepository;

    private List<PostComment> postCommentList = null;

    @BeforeEach
    void setUp() {
        postCommentRepository.deleteAll();

        postCommentList = new ArrayList<>();
        postCommentList.add(PostComment.builder().review("First PostComment").build());
        postCommentList.add(PostComment.builder().review("Second PostComment").build());
        postCommentList.add(PostComment.builder().review("Third PostComment").build());
        postCommentList = postCommentRepository.saveAll(postCommentList);
    }

    @Test
    void shouldFetchAllPostComments() throws Exception {
        this.mockMvc
                .perform(get("/api/postcomments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postCommentList.size())));
    }

    @Test
    void shouldFindPostCommentById() throws Exception {
        PostComment postComment = postCommentList.get(0);
        Long postCommentId = postComment.getId();

        this.mockMvc
                .perform(get("/api/postcomments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review", is(postComment.getReview())));
    }

    @Test
    void shouldCreateNewPostComment() throws Exception {
        PostComment postComment = PostComment.builder().review("New PostComment").build();
        this.mockMvc
                .perform(
                        post("/api/postcomments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postComment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.review", is(postComment.getReview())));
    }

    @Test
    void shouldUpdatePostComment() throws Exception {
        PostComment postComment = postCommentList.get(0);
        postComment.setReview("Updated PostComment");

        this.mockMvc
                .perform(
                        put("/api/postcomments/{id}", postComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review", is(postComment.getReview())));
    }

    @Test
    void shouldDeletePostComment() throws Exception {
        PostComment postComment = postCommentList.get(0);

        this.mockMvc
                .perform(delete("/api/postcomments/{id}", postComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review", is(postComment.getReview())));
    }
}
