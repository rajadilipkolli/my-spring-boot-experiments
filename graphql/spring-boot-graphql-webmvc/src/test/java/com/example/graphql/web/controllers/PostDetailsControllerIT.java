package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.repositories.PostDetailsRepository;
import com.example.graphql.repositories.PostRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@Disabled
class PostDetailsControllerIT extends AbstractIntegrationTest {

    @Autowired private PostDetailsRepository postDetailsRepository;

    @Autowired private PostRepository postRepository;

    private List<PostDetailsEntity> postDetailsList = null;

    private PostEntity post;

    @BeforeEach
    void setUp() {
        postDetailsRepository.deleteAll();
        postRepository.deleteAll();

        post = PostEntity.builder().content("First Post").build();

        postDetailsList = new ArrayList<>();
        postDetailsList.add(PostDetailsEntity.builder().createdBy("Junit1").build());
        postDetailsList.add(PostDetailsEntity.builder().createdBy("Junit2").build());
        postDetailsList.add(PostDetailsEntity.builder().createdBy("Junit3").build());
        postDetailsList.forEach(c -> post.setDetails(c));
        postRepository.save(post);
        postDetailsList = this.postDetailsRepository.findAll();
    }

    @Test
    void shouldFetchAllPostDetails() throws Exception {
        this.mockMvc
                .perform(get("/api/postdetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postDetailsList.size())));
    }

    @Test
    void shouldFindPostDetailsById() throws Exception {
        PostDetailsEntity postDetails = postDetailsList.get(0);
        Long postDetailsId = postDetails.getId();

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    void shouldCreateNewPostDetails() throws Exception {
        PostDetailsEntity postDetails = PostDetailsEntity.builder().createdBy("Junit1").build();
        this.mockMvc
                .perform(
                        post("/api/postdetails")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    @Disabled
    void shouldUpdatePostDetails() throws Exception {
        PostDetailsEntity postDetails = postDetailsList.get(0);
        postDetails.setCreatedBy("Updated PostDetails");

        this.mockMvc
                .perform(
                        put("/api/postdetails/{id}", postDetails.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    void shouldDeletePostDetails() throws Exception {
        PostDetailsEntity postDetails = postDetailsList.get(0);

        this.mockMvc
                .perform(delete("/api/postdetails/{id}", postDetails.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }
}
