package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.repositories.PostRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PostEntityControllerIT extends AbstractIntegrationTest {

    @Autowired private PostRepository postRepository;

    private List<PostEntity> postEntityList = null;

    @BeforeEach
    void setUp() {
        postRepository.deleteAllInBatch();

        postEntityList = new ArrayList<>();
        postEntityList.add(PostEntity.builder().content("First Post").build());
        postEntityList.add(PostEntity.builder().content("Second Post").build());
        postEntityList.add(PostEntity.builder().content("Third Post").build());
        postEntityList = postRepository.saveAll(postEntityList);
    }

    @Test
    @Disabled
    void shouldFetchAllPosts() throws Exception {
        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postEntityList.size())));
    }

    @Test
    @Disabled
    void shouldFindPostById() throws Exception {
        PostEntity postEntity = postEntityList.get(0);
        Long postId = postEntity.getId();

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.getContent())));
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        PostEntity postEntity = PostEntity.builder().content("New Post").build();
        this.mockMvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is(postEntity.getContent())));
    }

    @Test
    void shouldUpdatePost() throws Exception {
        PostEntity postEntity = postEntityList.get(0);
        postEntity.setContent("Updated Post");

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", postEntity.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.getContent())));
    }

    @Test
    @Disabled
    void shouldDeletePost() throws Exception {
        PostEntity postEntity = postEntityList.get(0);

        this.mockMvc
                .perform(delete("/api/posts/{id}", postEntity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.getContent())));
    }
}
