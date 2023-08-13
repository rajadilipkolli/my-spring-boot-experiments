package com.example.bootr2dbc.web.controllers;

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

import com.example.bootr2dbc.common.AbstractIntegrationTest;
import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.repositories.ReactivePostRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ReactivePostControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ReactivePostRepository reactivePostRepository;

    private List<ReactivePost> reactivePostList = null;

    @BeforeEach
    void setUp() {
        reactivePostRepository.deleteAllInBatch();

        reactivePostList = new ArrayList<>();
        reactivePostList.add(new ReactivePost(null, "First ReactivePost"));
        reactivePostList.add(new ReactivePost(null, "Second ReactivePost"));
        reactivePostList.add(new ReactivePost(null, "Third ReactivePost"));
        reactivePostList = reactivePostRepository.saveAll(reactivePostList);
    }

    @Test
    void shouldFetchAllReactivePosts() throws Exception {
        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(reactivePostList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindReactivePostById() throws Exception {
        ReactivePost reactivePost = reactivePostList.get(0);
        Long reactivePostId = reactivePost.getId();

        this.mockMvc
                .perform(get("/api/posts/{id}", reactivePostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reactivePost.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }

    @Test
    void shouldCreateNewReactivePost() throws Exception {
        ReactivePost reactivePost = new ReactivePost(null, "New ReactivePost");
        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivePost)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewReactivePostWithoutText() throws Exception {
        ReactivePost reactivePost = new ReactivePost(null, null);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivePost)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateReactivePost() throws Exception {
        ReactivePost reactivePost = reactivePostList.get(0);
        reactivePost.setText("Updated ReactivePost");

        this.mockMvc
                .perform(put("/api/posts/{id}", reactivePost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivePost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reactivePost.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }

    @Test
    void shouldDeleteReactivePost() throws Exception {
        ReactivePost reactivePost = reactivePostList.get(0);

        this.mockMvc
                .perform(delete("/api/posts/{id}", reactivePost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reactivePost.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }
}
