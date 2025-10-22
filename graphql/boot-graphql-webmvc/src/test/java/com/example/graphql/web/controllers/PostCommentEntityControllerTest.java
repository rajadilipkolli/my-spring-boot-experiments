package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.services.PostCommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostCommentController.class)
@ActiveProfiles(PROFILE_TEST)
class PostCommentEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostCommentService postCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<PostCommentResponse> postCommentResponseList;

    @BeforeEach
    void setUp() {
        this.postCommentResponseList = new ArrayList<>();
        this.postCommentResponseList.add(PostCommentResponse.builder()
                .commentId(1L)
                .postId(100L)
                .title("First PostComment")
                .build());
        this.postCommentResponseList.add(PostCommentResponse.builder()
                .commentId(2L)
                .title("Second PostComment")
                .build());
        this.postCommentResponseList.add(PostCommentResponse.builder()
                .commentId(3L)
                .title("Third PostComment")
                .build());
    }

    @Test
    void shouldFetchAllPostComments() throws Exception {
        given(postCommentService.findAllPostComments()).willReturn(this.postCommentResponseList);

        this.mockMvc
                .perform(get("/api/postcomments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(postCommentResponseList.size())));
    }

    @Test
    void shouldFindPostCommentById() throws Exception {
        PostCommentResponse postCommentResponse = postCommentResponseList.getFirst();
        Long postCommentId = postCommentResponse.postId();
        given(postCommentService.findPostCommentById(postCommentId))
                .willReturn(Optional.of(postCommentResponseList.getFirst()));

        this.mockMvc
                .perform(get("/api/postcomments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is(postCommentResponse.title())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPostComment() throws Exception {
        Long postCommentId = 1L;
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/postcomments/{id}", postCommentId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPostComment() throws Exception {
        PostCommentResponse postCommentResponse = new PostCommentResponse(
                1L, 100L, "First PostComment", "First Content", true, OffsetDateTime.now(), LocalDateTime.now());

        PostCommentRequest postCommentRequest = new PostCommentRequest("First PostComment", "First Content", "1", true);

        given(postCommentService.addCommentToPost(postCommentRequest)).willReturn(postCommentResponse);

        this.mockMvc
                .perform(post("/api/postcomments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId", is(1)))
                .andExpect(jsonPath("$.commentId", is(100)))
                .andExpect(jsonPath("$.title", is(postCommentResponse.title())))
                .andExpect(jsonPath("$.content", is(postCommentResponse.content())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostCommentWithoutText() throws Exception {
        PostCommentRequest postComment = new PostCommentRequest(null, null, null, null);

        this.mockMvc
                .perform(post("/api/postcomments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postComment)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("content")))
                .andExpect(jsonPath("$.violations[0].message", is("CommentContent must not be blank")))
                .andExpect(jsonPath("$.violations[1].field", is("postId")))
                .andExpect(jsonPath("$.violations[1].message", is("PostId must must not be blank and greater than 0")))
                .andExpect(jsonPath("$.violations[2].field", is("title")))
                .andExpect(jsonPath("$.violations[2].message", is("CommentTitle must not be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdatePostComment() throws Exception {
        PostCommentResponse postCommentResponse = postCommentResponseList.getFirst();
        Long postCommentId = postCommentResponse.postId();
        PostCommentRequest postCommentRequest =
                new PostCommentRequest("First Title", "First Content", String.valueOf(postCommentId), true);
        PostCommentEntity postCommentEntity = new PostCommentEntity();
        postCommentEntity.setTitle("UpdatedTitle");
        given(postCommentService.findCommentById(postCommentId)).willReturn(Optional.of(postCommentEntity));
        given(postCommentService.updatePostComment(postCommentEntity, postCommentRequest))
                .willReturn(postCommentResponse);

        this.mockMvc
                .perform(put("/api/postcomments/{id}", postCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is(postCommentResponse.title())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPostComment() throws Exception {
        Long postCommentId = 1L;
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.empty());
        PostCommentEntity postCommentEntity =
                new PostCommentEntity().setId(postCommentId).setTitle("Updated PostComment");

        this.mockMvc
                .perform(put("/api/postcomments/{id}", postCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentEntity)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePostComment() throws Exception {
        PostCommentResponse postCommentResponse = postCommentResponseList.getFirst();
        Long postCommentId = postCommentResponse.postId();
        given(postCommentService.findPostCommentById(postCommentId))
                .willReturn(Optional.of(postCommentResponseList.getFirst()));
        doNothing().when(postCommentService).deletePostCommentById(postCommentId);

        this.mockMvc
                .perform(delete("/api/postcomments/{id}", postCommentId))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPostComment() throws Exception {
        Long postCommentId = 1L;
        given(postCommentService.findPostCommentById(postCommentId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/postcomments/{id}", postCommentId)).andExpect(status().isNotFound());
    }
}
