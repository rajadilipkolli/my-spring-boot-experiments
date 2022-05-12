package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostDetails;
import com.example.graphql.repositories.PostDetailsRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PostDetailsControllerIT extends AbstractIntegrationTest {

    @Autowired private PostDetailsRepository postDetailsRepository;

    private List<PostDetails> postDetailsList = null;

    @BeforeEach
    void setUp() {
        postDetailsRepository.deleteAll();

        postDetailsList = new ArrayList<>();
        postDetailsList.add(new PostDetails(1L, "First PostDetails"));
        postDetailsList.add(new PostDetails(2L, "Second PostDetails"));
        postDetailsList.add(new PostDetails(3L, "Third PostDetails"));
        postDetailsList = postDetailsRepository.saveAll(postDetailsList);
    }

    @Test
    void shouldFetchAllPostDetailss() throws Exception {
        this.mockMvc
                .perform(get("/api/postdetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postDetailsList.size())));
    }

    @Test
    void shouldFindPostDetailsById() throws Exception {
        PostDetails postDetails = postDetailsList.get(0);
        Long postDetailsId = postDetails.getId();

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldCreateNewPostDetails() throws Exception {
        PostDetails postDetails = new PostDetails(null, "New PostDetails");
        this.mockMvc
                .perform(
                        post("/api/postdetails")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostDetailsWithoutText() throws Exception {
        PostDetails postDetails = new PostDetails(null, null);

        this.mockMvc
                .perform(
                        post("/api/postdetails")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePostDetails() throws Exception {
        PostDetails postDetails = postDetailsList.get(0);
        postDetails.setText("Updated PostDetails");

        this.mockMvc
                .perform(
                        put("/api/postdetails/{id}", postDetails.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldDeletePostDetails() throws Exception {
        PostDetails postDetails = postDetailsList.get(0);

        this.mockMvc
                .perform(delete("/api/postdetails/{id}", postDetails.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }
}
