package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.Post;
import com.example.graphql.services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostController.class)
@ActiveProfiles(PROFILE_TEST)
class PostControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PostService postService;

    @Autowired private ObjectMapper objectMapper;

    private List<Post> postList;

    @BeforeEach
    void setUp() {
        this.postList = new ArrayList<>();
        this.postList.add(Post.builder().id(1L).content("First Post").build());
        this.postList.add(Post.builder().id(2L).content("Second Post").build());
        this.postList.add(Post.builder().id(3L).content("Third Post").build());
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
        given(postService.findAllPosts()).willReturn(this.postList);

        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postList.size())));
    }

    @Test
    void shouldFindPostById() throws Exception {
        Long postId = 1L;
        Post post = Post.builder().id(postId).content("First Post").build();
        given(postService.findPostById(postId)).willReturn(Optional.of(post));

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(post.getContent())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        given(postService.savePost(any(Post.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Post post = Post.builder().id(1L).content("First Post").build();
        this.mockMvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.content", is(post.getContent())));
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = 1L;
        Post post = Post.builder().id(postId).content("Updated Post").build();
        given(postService.findPostById(postId)).willReturn(Optional.of(post));
        given(postService.savePost(any(Post.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", post.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(post.getContent())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());
        Post post = Post.builder().id(postId).content("Updated Post").build();

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePost() throws Exception {
        Long postId = 1L;
        Post post = Post.builder().id(postId).content("First Post").build();
        given(postService.findPostById(postId)).willReturn(Optional.of(post));
        doNothing().when(postService).deletePostById(post.getId());

        this.mockMvc
                .perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(post.getContent())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }
}
