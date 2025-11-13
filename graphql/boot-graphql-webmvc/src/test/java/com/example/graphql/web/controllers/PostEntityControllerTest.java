package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
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

import com.example.graphql.entities.PostEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.services.PostService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PostController.class)
@ActiveProfiles(PROFILE_TEST)
class PostEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<PostEntity> postEntityList;

    private final List<PostResponse> postResponseList = List.of(
            new PostResponse(null, "First Post", false, null, null, null, null, new ArrayList<>()),
            new PostResponse(null, "Second Post", false, null, null, null, null, new ArrayList<>()),
            new PostResponse(null, "Third Post", false, null, null, null, null, new ArrayList<>()));

    @BeforeEach
    void setUp() {
        this.postEntityList = new ArrayList<>();

        this.postEntityList.add(new PostEntity().setId(1L).setContent("First Post"));
        this.postEntityList.add(new PostEntity().setId(2L).setContent("Second Post"));
        this.postEntityList.add(new PostEntity().setId(3L).setContent("Third Post"));
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
        given(postService.findPostById(postId)).willReturn(Optional.of(postResponseList.getFirst()));

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("First Post")));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Post: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/posts/1")));
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        given(postService.savePost(any(NewPostRequest.class))).willReturn(postResponseList.getFirst());

        NewPostRequest postEntity = new NewPostRequest(
                "First Title",
                "First Post",
                "junit1@email.com",
                false,
                new PostDetailsRequest("detailsKey", "JunitCreatedBy"),
                null);
        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutValidData() throws Exception {
        NewPostRequest post = new NewPostRequest(null, null, null, false, null, null);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("content")))
                .andExpect(jsonPath("$.violations[0].message", is("PostContent must not be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("email")))
                .andExpect(jsonPath("$.violations[1].message", is("Email must not be blank")))
                .andExpect(jsonPath("$.violations[2].field", is("title")))
                .andExpect(jsonPath("$.violations[2].message", is("PostTitle must not be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = 1L;
        NewPostRequest postEntity = new NewPostRequest(
                "First Title",
                "Updated Content",
                "junit1@email.com",
                false,
                new PostDetailsRequest("detailsKey", "JunitCreatedBy"),
                null);
        PostResponse value =
                new PostResponse(null, "Updated Content", false, null, null, null, null, new ArrayList<>());
        given(postService.updatePost(postId, postEntity)).willReturn(Optional.of(value));

        this.mockMvc
                .perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());
        PostEntity postEntity = new PostEntity().setId(postId).setContent("Updated Post");

        this.mockMvc
                .perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEntity)))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Post: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/posts/1")));
    }

    @Test
    void shouldDeletePost() throws Exception {
        Long postId = 1L;
        PostEntity postEntity = new PostEntity().setId(postId).setContent("First Post");
        given(postService.findPostById(postId)).willReturn(Optional.of(postResponseList.getFirst()));
        doNothing().when(postService).deletePostById(postEntity.getId());

        this.mockMvc.perform(delete("/api/posts/{id}", postEntity.getId())).andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Post: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/posts/1")));
    }
}
