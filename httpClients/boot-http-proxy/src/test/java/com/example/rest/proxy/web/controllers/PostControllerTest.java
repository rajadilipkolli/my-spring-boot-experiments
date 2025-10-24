package com.example.rest.proxy.web.controllers;

import static com.example.rest.proxy.utils.AppConstants.PROFILE_TEST;
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

import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.model.response.PostResponse;
import com.example.rest.proxy.services.PostService;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PostController.class)
@ActiveProfiles(PROFILE_TEST)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFindPostById() throws Exception {
        Long postId = 1L;
        PostResponse post = new PostResponse(postId, 1L, "text 1", "First Body", new ArrayList<>());
        given(postService.findPostById(postId)).willReturn(Optional.of(post));

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post.title())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        Post post = new Post().setId(1L).setTitle("some text").setUserId(1L).setBody("First Body");
        given(postService.savePost(any(Post.class)))
                .willReturn(new PostResponse(1L, 1L, "some text", "First Body", new ArrayList<>()));

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId", notNullValue()))
                .andExpect(jsonPath("$.title", is(post.getTitle())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitleAndBody() throws Exception {
        Post post = new Post();

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.http-proxy.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[1].field", is("title")))
                .andExpect(jsonPath("$.violations[1].message", is("Title cannot be empty")))
                .andExpect(jsonPath("$.violations[0].field", is("body")))
                .andExpect(jsonPath("$.violations[0].message", is("Body cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = 1L;
        Post post =
                new Post().setId(postId).setTitle("Updated text").setUserId(1L).setBody("First Body");
        PostResponse postResponse = new PostResponse(postId, 1L, "Updated text", "First Body", new ArrayList<>());
        given(postService.findPostById(postId)).willReturn(Optional.of(postResponse));
        given(postService.saveAndConvertToResponse(any(Post.class))).willReturn(postResponse);

        this.mockMvc
                .perform(put("/api/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post.getTitle())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());
        Post post =
                new Post().setId(postId).setTitle("Updated text").setUserId(1L).setBody("First Body");

        this.mockMvc
                .perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePost() throws Exception {
        Long postId = 1L;
        PostResponse post = new PostResponse(postId, 1L, "Some text", "First Body", new ArrayList<>());
        given(postService.findPostById(postId)).willReturn(Optional.of(post));
        doNothing().when(postService).deletePostById(post.postId());

        this.mockMvc
                .perform(delete("/api/posts/{id}", post.postId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post.title())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }
}
