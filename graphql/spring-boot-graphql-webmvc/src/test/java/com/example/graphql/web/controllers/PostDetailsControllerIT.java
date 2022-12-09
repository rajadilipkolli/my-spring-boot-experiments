package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.Post;
import com.example.graphql.entities.PostDetails;
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

    private List<PostDetails> postDetailsList = null;

    private Post post;

    @BeforeEach
    void setUp() {
        postDetailsRepository.deleteAll();
        postRepository.deleteAll();

        post = Post.builder().id(1L).content("First Post").build();

        postDetailsList = new ArrayList<>();
        postDetailsList.add(PostDetails.builder().id(1L).createdBy("Junit1").build());
        postDetailsList.add(PostDetails.builder().id(2L).createdBy("Junit2").build());
        postDetailsList.add(PostDetails.builder().id(3L).createdBy("Junit3").build());
        postDetailsList.forEach(c -> post.addDetails(c));
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
        PostDetails postDetails = postDetailsList.get(0);
        Long postDetailsId = postDetails.getId();

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    void shouldCreateNewPostDetails() throws Exception {
        PostDetails postDetails = PostDetails.builder().createdBy("Junit1").build();
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
        PostDetails postDetails = postDetailsList.get(0);
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
        PostDetails postDetails = postDetailsList.get(0);

        this.mockMvc
                .perform(delete("/api/postdetails/{id}", postDetails.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }
}
