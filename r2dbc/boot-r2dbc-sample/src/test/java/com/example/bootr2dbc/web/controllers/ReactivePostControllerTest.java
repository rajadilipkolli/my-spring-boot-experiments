package com.example.bootr2dbc.web.controllers;

import static com.example.bootr2dbc.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.model.response.PagedResult;
import com.example.bootr2dbc.services.ReactivePostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ReactivePostController.class)
@ActiveProfiles(PROFILE_TEST)
class ReactivePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReactivePostService reactivePostService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<ReactivePost> reactivePostList;

    @BeforeEach
    void setUp() {
        this.reactivePostList = new ArrayList<>();
        this.reactivePostList.add(new ReactivePost(1L, "text 1"));
        this.reactivePostList.add(new ReactivePost(2L, "text 2"));
        this.reactivePostList.add(new ReactivePost(3L, "text 3"));
    }

    @Test
    void shouldFetchAllReactivePosts() throws Exception {
        Page<ReactivePost> page = new PageImpl<>(reactivePostList);
        PagedResult<ReactivePost> reactivePostPagedResult = new PagedResult<>(page);
        given(reactivePostService.findAllReactivePosts(0, 10, "id", "asc")).willReturn(reactivePostPagedResult);

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
        Long reactivePostId = 1L;
        ReactivePost reactivePost = new ReactivePost(reactivePostId, "text 1");
        given(reactivePostService.findReactivePostById(reactivePostId)).willReturn(Optional.of(reactivePost));

        this.mockMvc
                .perform(get("/api/posts/{id}", reactivePostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingReactivePost() throws Exception {
        Long reactivePostId = 1L;
        given(reactivePostService.findReactivePostById(reactivePostId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/posts/{id}", reactivePostId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewReactivePost() throws Exception {
        given(reactivePostService.saveReactivePost(any(ReactivePost.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        ReactivePost reactivePost = new ReactivePost(1L, "some text");
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
        Long reactivePostId = 1L;
        ReactivePost reactivePost = new ReactivePost(reactivePostId, "Updated text");
        given(reactivePostService.findReactivePostById(reactivePostId)).willReturn(Optional.of(reactivePost));
        given(reactivePostService.saveReactivePost(any(ReactivePost.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(put("/api/posts/{id}", reactivePost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivePost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingReactivePost() throws Exception {
        Long reactivePostId = 1L;
        given(reactivePostService.findReactivePostById(reactivePostId)).willReturn(Optional.empty());
        ReactivePost reactivePost = new ReactivePost(reactivePostId, "Updated text");

        this.mockMvc
                .perform(put("/api/posts/{id}", reactivePostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivePost)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteReactivePost() throws Exception {
        Long reactivePostId = 1L;
        ReactivePost reactivePost = new ReactivePost(reactivePostId, "Some text");
        given(reactivePostService.findReactivePostById(reactivePostId)).willReturn(Optional.of(reactivePost));
        doNothing().when(reactivePostService).deleteReactivePostById(reactivePost.getId());

        this.mockMvc
                .perform(delete("/api/posts/{id}", reactivePost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(reactivePost.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingReactivePost() throws Exception {
        Long reactivePostId = 1L;
        given(reactivePostService.findReactivePostById(reactivePostId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/posts/{id}", reactivePostId)).andExpect(status().isNotFound());
    }
}
