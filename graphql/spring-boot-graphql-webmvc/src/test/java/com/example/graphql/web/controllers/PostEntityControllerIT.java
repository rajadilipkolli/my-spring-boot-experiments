package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.repositories.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

class PostEntityControllerIT extends AbstractIntegrationTest {

    @Autowired private PostRepository postRepository;

    private List<PostEntity> postEntityList = null;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        postEntityList = new ArrayList<>();
        PostEntity firstPost = PostEntity.builder().content("First Post").build();
        firstPost.setDetails(PostDetailsEntity.builder().detailsKey("Junit1").build());
        postEntityList.add(firstPost);
        PostEntity secondPost = PostEntity.builder().content("Second Post").build();
        secondPost.setDetails(PostDetailsEntity.builder().detailsKey("Junit2").build());
        postEntityList.add(secondPost);
        PostEntity thirdPost = PostEntity.builder().content("Third Post").build();
        thirdPost.setDetails(PostDetailsEntity.builder().detailsKey("Junit3").build());
        postEntityList.add(thirdPost);
        postEntityList = postRepository.saveAll(postEntityList);
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postEntityList.size())));
    }

    @Test
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
        NewPostRequest postEntity =
                new NewPostRequest(
                        "First Title",
                        "New Post",
                        "junit1@email.com",
                        false,
                        new PostDetailsRequest("detailsKey"),
                        null);

        this.mockMvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is(postEntity.content())))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void shouldUpdatePost() throws Exception {

        NewPostRequest postEntity =
                new NewPostRequest(
                        "First Title",
                        "Updated Post",
                        "junit1@email.com",
                        false,
                        new PostDetailsRequest("detailsKey"),
                        null);

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", postEntityList.get(0).getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldDeletePost() throws Exception {
        PostEntity postEntity = postEntityList.get(0);

        this.mockMvc
                .perform(delete("/api/posts/{id}", postEntity.getId()))
                .andExpect(status().isAccepted());
    }
}
