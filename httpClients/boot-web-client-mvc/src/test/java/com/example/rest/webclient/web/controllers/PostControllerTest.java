package com.example.rest.webclient.web.controllers;

import static com.example.rest.webclient.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.rest.webclient.model.response.PostDto;
import com.example.rest.webclient.services.PostService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PostService postService;

    @Autowired private ObjectMapper objectMapper;

    private List<PostDto> postList;

    @BeforeEach
    void setUp() {
        this.postList = new ArrayList<>();
        this.postList.add(new PostDto(1L, 1L, "text 1", "First Body"));
        this.postList.add(new PostDto(1L, 2L, "text 2", "Second Body"));
        this.postList.add(new PostDto(1L, 3L, "text 3", "Third Body"));
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
        given(postService.findAllPosts()).willReturn(postList);

        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postList.size())));
    }

    @Test
    void shouldFindPostById() throws Exception {
        Long postId = 1L;
        PostDto post = new PostDto(postId, 1L, "text 1", "First Body");
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
        given(postService.savePost(any(PostDto.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        PostDto post = new PostDto(1L, 1L, "text 1", "First Body");
        this.mockMvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is(post.title())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitle() throws Exception {
        PostDto post = new PostDto(null, null, null, null);

        this.mockMvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isBadRequest())
                .andExpect(
                        header().string(
                                        "Content-Type",
                                        is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://api.web-client-mvc.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("title")))
                .andExpect(jsonPath("$.violations[0].message", is("title can't be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = 1L;
        PostDto post = new PostDto(1L, postId, "Updated text", "First Body");
        given(postService.updatePostById(postId, post)).willReturn(Optional.of(post));

        this.mockMvc
                .perform(
                        put("/api/posts/{id}", post.id())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post.title())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());
        PostDto post = new PostDto(1L, postId, "Updated text", "First Body");

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
        PostDto post = new PostDto(1L, postId, "Some text", "First Body");
        given(postService.findPostById(postId)).willReturn(Optional.of(post));
        given(postService.deletePostById(post.id())).willReturn(post);

        this.mockMvc
                .perform(delete("/api/posts/{id}", post.id()))
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
