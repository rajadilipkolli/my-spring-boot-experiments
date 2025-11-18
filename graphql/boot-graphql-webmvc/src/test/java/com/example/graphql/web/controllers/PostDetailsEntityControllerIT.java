package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.model.request.PostDetailsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PostDetailsEntityControllerIT extends AbstractIntegrationTest {

    private PostEntity post;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        post = new PostEntity().setContent("First Post").setTitle("First Title");

        PostDetailsEntity postDetailsEntity = new PostDetailsEntity().setDetailsKey("Junit1");
        post.setDetails(postDetailsEntity);

        post = postRepository.save(post);
    }

    @Test
    void shouldFetchAllPostDetails() throws Exception {
        this.mockMvc
                .perform(get("/api/post/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldFindPostDetailsById() throws Exception {
        PostDetailsEntity postDetails = post.getDetails();

        this.mockMvc
                .perform(get("/api/post/details/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    void shouldUpdatePostDetails() throws Exception {

        PostDetailsRequest postDetailsRequest = new PostDetailsRequest("Updated PostDetails", "Junit");

        Long postDetailsId = post.getId();
        this.mockMvc
                .perform(put("/api/post/details/{id}", postDetailsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postDetailsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detailsKey", is("Updated PostDetails")));
    }
}
