package com.example.graphql.querydsl.web.controllers;

import static com.example.graphql.querydsl.utils.AppConstants.PROFILE_TEST;
import static com.example.graphql.querydsl.utils.AppConstants.formatterWithMillis;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostDetails;
import com.example.graphql.querydsl.exception.PostNotFoundException;
import com.example.graphql.querydsl.model.query.FindQuery;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.services.PostService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    private List<Post> postList;

    @BeforeEach
    void setUp() {
        this.postList = new ArrayList<>();
        this.postList.add(new Post()
                .setId(1L)
                .setTitle("title 1")
                .setContent("content 1")
                .setDetails(new PostDetails().setCreatedOn(getCreatedOn())));
        this.postList.add(new Post()
                .setId(2L)
                .setTitle("title 2")
                .setContent("content 2")
                .setDetails(new PostDetails().setCreatedOn(getCreatedOn())));
        this.postList.add(new Post()
                .setId(3L)
                .setTitle("title 3")
                .setContent("content 3")
                .setDetails(new PostDetails().setCreatedOn(getCreatedOn())));
    }

    @Test
    void shouldFetchAllPosts() throws Exception {

        Page<Post> page = new PageImpl<>(postList);
        PagedResult<PostResponse> postPagedResult = new PagedResult<>(page, getPostResponseList());
        FindQuery findPostsQuery = new FindQuery(0, 10, "id", "asc");
        given(postService.findAllPosts(findPostsQuery)).willReturn(postPagedResult);

        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(postList.size())))
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
        PostResponse post =
                new PostResponse(postId, "text 1", "content 1", getCreatedOn(), new ArrayList<>(), new ArrayList<>());
        given(postService.findPostById(postId)).willReturn(Optional.of(post));

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post.title())))
                .andExpect(jsonPath("$.content", is(post.content())))
                .andExpect(
                        jsonPath("$.createdOn", is(post.createdOn().format(formatterWithMillis)), LocalDateTime.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Post with Id '%d' not found".formatted(postId)));
    }

    @Test
    void shouldCreateNewPost() throws Exception {

        PostResponse post =
                new PostResponse(1L, "some text", "some content", getCreatedOn(), new ArrayList<>(), new ArrayList<>());
        CreatePostRequest postRequest =
                new CreatePostRequest("some title", "some content", "appUser", new ArrayList<>(), new ArrayList<>());
        given(postService.savePost(any(CreatePostRequest.class))).willReturn(post);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is(post.title())))
                .andExpect(jsonPath("$.content", is(post.content())))
                .andExpect(
                        jsonPath("$.createdOn", is(post.createdOn().format(formatterWithMillis)), LocalDateTime.class));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitleAndContent() throws Exception {
        CreatePostRequest createPostRequest = new CreatePostRequest(null, null, null, null, null);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts")))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("content")))
                .andExpect(jsonPath("$.violations[0].message", is("Content cannot be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("createdBy")))
                .andExpect(jsonPath("$.violations[1].message", is("CreatedBy cannot be blank")))
                .andExpect(jsonPath("$.violations[2].field", is("title")))
                .andExpect(jsonPath("$.violations[2].message", is("Title cannot be empty")))
                .andReturn();
    }

    @Nested
    @DisplayName("update Method")
    class Update {
        @Test
        void shouldUpdatePost() throws Exception {
            Long postId = 1L;
            PostResponse post = new PostResponse(
                    postId, "Updated text", "some content", getCreatedOn(), new ArrayList<>(), new ArrayList<>());
            UpdatePostRequest updatePostRequest = new UpdatePostRequest("Updated text", "some content");
            given(postService.updatePost(eq(postId), any(UpdatePostRequest.class)))
                    .willReturn(post);

            mockMvc.perform(put("/api/posts/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePostRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(postId), Long.class))
                    .andExpect(jsonPath("$.title", is(post.title())))
                    .andExpect(jsonPath("$.content", is(post.content())))
                    .andExpect(jsonPath(
                            "$.createdOn", is(post.createdOn().format(formatterWithMillis)), LocalDateTime.class));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistingPost() throws Exception {
            Long postId = 1L;
            UpdatePostRequest updatePostRequest = new UpdatePostRequest("Updated text", "some content");
            given(postService.updatePost(eq(postId), any(UpdatePostRequest.class)))
                    .willThrow(new PostNotFoundException(postId));

            mockMvc.perform(put("/api/posts/{id}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePostRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                    .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                    .andExpect(jsonPath("$.title", is("Not Found")))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.detail").value("Post with Id '%d' not found".formatted(postId)));
        }
    }

    @Test
    void shouldDeletePost() throws Exception {
        Long postId = 1L;
        PostResponse post = new PostResponse(
                postId, "Some text", "some content", getCreatedOn(), new ArrayList<>(), new ArrayList<>());
        given(postService.findPostById(postId)).willReturn(Optional.of(post));
        doNothing().when(postService).deletePostById(postId);

        this.mockMvc
                .perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post.title())))
                .andExpect(jsonPath("$.content", is(post.content())))
                .andExpect(
                        jsonPath("$.createdOn", is(post.createdOn().format(formatterWithMillis)), LocalDateTime.class));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPost() throws Exception {
        Long postId = 1L;
        given(postService.findPostById(postId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/posts/{id}", postId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("Post with Id '%d' not found".formatted(postId)));
    }

    List<PostResponse> getPostResponseList() {
        return postList.stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getDetails().getCreatedOn(),
                        new ArrayList<>(),
                        new ArrayList<>()))
                .toList();
    }

    private LocalDateTime getCreatedOn() {
        return LocalDateTime.of(2023, 12, 31, 10, 35, 45, 99);
    }
}
