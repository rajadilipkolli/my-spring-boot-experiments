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
import com.example.graphql.model.query.FindQuery;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PagedResult;
import com.example.graphql.model.response.PostResponse;
import com.example.graphql.services.PostService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = PostController.class)
@ActiveProfiles(PROFILE_TEST)
class PostEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private JsonMapper jsonMapper;

    private List<PostEntity> postEntityList;

    private final List<PostResponse> postResponseList = List.of(
            new PostResponse(1L, null, "First Post", false, null, null, null, null, new ArrayList<>()),
            new PostResponse(2L, null, "Second Post", false, null, null, null, null, new ArrayList<>()),
            new PostResponse(3L, null, "Third Post", false, null, null, null, null, new ArrayList<>()));

    @BeforeEach
    void setUp() {
        this.postEntityList = new ArrayList<>();

        this.postEntityList.add(new PostEntity().setId(1L).setContent("First Post"));
        this.postEntityList.add(new PostEntity().setId(2L).setContent("Second Post"));
        this.postEntityList.add(new PostEntity().setId(3L).setContent("Third Post"));
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
        FindQuery findQuery = new FindQuery(0, 10, "id", "asc");
        Page<PostEntity> page = new PageImpl<>(postEntityList);
        PagedResult<PostResponse> postPagedResult = new PagedResult<>(page, postResponseList);
        given(postService.findAllPosts(findQuery)).willReturn(postPagedResult);

        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(postResponseList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
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
                        .content(jsonMapper.writeValueAsString(postEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutValidData() throws Exception {
        NewPostRequest post = new NewPostRequest(null, null, null, false, null, null);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(post)))
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
                new PostResponse(1L, null, "Updated Content", false, null, null, null, null, new ArrayList<>());
        given(postService.updatePost(postId, postEntity)).willReturn(Optional.of(value));

        this.mockMvc
                .perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.content())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());
        NewPostRequest newPostRequest = new NewPostRequest(
                "First Title",
                "Updated Content",
                "junit1@email.com",
                false,
                new PostDetailsRequest("detailsKey", "JunitCreatedBy"),
                null);

        this.mockMvc
                .perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newPostRequest)))
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
        given(postService.existsPostById(postId)).willReturn(true);
        doNothing().when(postService).deletePostById(postId);

        this.mockMvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.existsPostById(postId)).willReturn(false);

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
