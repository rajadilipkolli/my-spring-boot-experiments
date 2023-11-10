package com.example.graphql.querydsl.web.controllers;

import static com.example.graphql.querydsl.utils.AppConstants.PROFILE_TEST;
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

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.exception.PostCommentNotFoundException;
import com.example.graphql.querydsl.model.query.FindPostCommentsQuery;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.response.PagedResult;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import com.example.graphql.querydsl.services.PostCommentService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostCommentController.class)
@ActiveProfiles(PROFILE_TEST)
class PostCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostCommentService postCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<PostComment> postCommentList;

    @BeforeEach
    void setUp() {
        this.postCommentList = new ArrayList<>();
        this.postCommentList.add(new PostComment(1L, "text 1"));
        this.postCommentList.add(new PostComment(2L, "text 2"));
        this.postCommentList.add(new PostComment(3L, "text 3"));
    }

    @Test
    void shouldFetchAllPostComments() throws Exception {

        Page<PostComment> page = new PageImpl<>(postCommentList);
        PagedResult<PostCommentResponse> postCommentPagedResult = new PagedResult<>(page, getPostCommentResponseList());
        FindPostCommentsQuery findPostCommentsQuery = new FindPostCommentsQuery(0, 10, "id", "asc");
        given(postCommentService.findAllPostComments(findPostCommentsQuery)).willReturn(postCommentPagedResult);

        this.mockMvc
                .perform(get("/api/posts/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(postCommentList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindPostCommentById() throws Exception {
        Long postCommentId = 1L;
        PostCommentResponse postComment = new PostCommentResponse(postCommentId, "text 1");
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.of(postComment));

        this.mockMvc
                .perform(get("/api/posts/comments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postComment.text())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPostComment() throws Exception {
        Long postCommentId = 1L;
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/posts/comments/{id}", postCommentId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("PostComment with Id '%d' not found".formatted(postCommentId)));
    }

    @Test
    void shouldCreateNewPostComment() throws Exception {

        PostCommentResponse postComment = new PostCommentResponse(1L, "some text");
        PostCommentRequest postCommentRequest = new PostCommentRequest("some text");
        given(postCommentService.savePostComment(any(PostCommentRequest.class))).willReturn(postComment);

        this.mockMvc
                .perform(post("/api/posts/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(postComment.text())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostCommentWithoutText() throws Exception {
        PostCommentRequest postCommentRequest = new PostCommentRequest(null);

        this.mockMvc
                .perform(post("/api/posts/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts/comments")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePostComment() throws Exception {
        Long postCommentId = 1L;
        PostCommentResponse postComment = new PostCommentResponse(postCommentId, "Updated text");
        PostCommentRequest postCommentRequest = new PostCommentRequest("Updated text");
        given(postCommentService.updatePostComment(eq(postCommentId), any(PostCommentRequest.class)))
                .willReturn(postComment);

        this.mockMvc
                .perform(put("/api/posts/comments/{id}", postCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postCommentId), Long.class))
                .andExpect(jsonPath("$.text", is(postComment.text())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPostComment() throws Exception {
        Long postCommentId = 1L;
        PostCommentRequest postCommentRequest = new PostCommentRequest("Updated text");
        given(postCommentService.updatePostComment(eq(postCommentId), any(PostCommentRequest.class)))
                .willThrow(new PostCommentNotFoundException(postCommentId));

        this.mockMvc
                .perform(put("/api/posts/comments/{id}", postCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("PostComment with Id '%d' not found".formatted(postCommentId)));
    }

    @Test
    void shouldDeletePostComment() throws Exception {
        Long postCommentId = 1L;
        PostCommentResponse postComment = new PostCommentResponse(postCommentId, "Some text");
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.of(postComment));
        doNothing().when(postCommentService).deletePostCommentById(postCommentId);

        this.mockMvc
                .perform(delete("/api/posts/comments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postComment.text())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPostComment() throws Exception {
        Long postCommentId = 1L;
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/posts/comments/{id}", postCommentId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-graphql-querydsl.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("PostComment with Id '%d' not found".formatted(postCommentId)));
    }

    List<PostCommentResponse> getPostCommentResponseList() {
        return postCommentList.stream()
                .map(postComment -> new PostCommentResponse(postComment.getId(), postComment.getText()))
                .toList();
    }
}
