package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.PostEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = PostController.class)
@ActiveProfiles(PROFILE_TEST)
class PostEntityControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PostService postService;

    @Autowired private ObjectMapper objectMapper;

    private List<PostEntity> postEntityList;

    private List<PostResponse> postResponseList =
            List.of(
                    new PostResponse(null, "First Post", false, null, null, null, null),
                    new PostResponse(null, "Second Post", false, null, null, null, null),
                    new PostResponse(null, "Third Post", false, null, null, null, null));

    @BeforeEach
    void setUp() {
        this.postEntityList = new ArrayList<>();
        this.postEntityList.add(PostEntity.builder().id(1L).content("First Post").build());
        this.postEntityList.add(PostEntity.builder().id(2L).content("Second Post").build());
        this.postEntityList.add(PostEntity.builder().id(3L).content("Third Post").build());
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
        given(postService.findAllPosts()).willReturn(this.postResponseList);

        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postEntityList.size())));
    }

    @Test
    void shouldFindPostById() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.of(postResponseList.get(0)));

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("First Post")));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        given(postService.savePost(any(NewPostRequest.class))).willReturn(postResponseList.get(0));

        NewPostRequest postEntity =
                new NewPostRequest(
                        "First Title",
                        "First Post",
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
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = 1L;
        NewPostRequest postEntity =
                new NewPostRequest(
                        "First Title",
                        "Updated Content",
                        "junit1@email.com",
                        false,
                        new PostDetailsRequest("detailsKey"),
                        null);
        PostResponse value =
                new PostResponse(null, "Updated Content", false, null, null, null, null);
        given(postService.updatePost(postId, postEntity)).willReturn(Optional.of(value));

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());
        PostEntity postEntity = PostEntity.builder().id(postId).content("Updated Post").build();

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePost() throws Exception {
        Long postId = 1L;
        PostEntity postEntity = PostEntity.builder().id(postId).content("First Post").build();
        given(postService.findPostById(postId)).willReturn(Optional.of(postResponseList.get(0)));
        doNothing().when(postService).deletePostById(postEntity.getId());

        this.mockMvc
                .perform(delete("/api/posts/{id}", postEntity.getId()))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }
}
