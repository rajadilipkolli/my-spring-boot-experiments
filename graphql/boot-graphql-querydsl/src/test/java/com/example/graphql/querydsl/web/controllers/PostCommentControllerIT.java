package com.example.graphql.querydsl.web.controllers;

import static com.example.graphql.querydsl.utils.TestData.getPost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.repositories.PostCommentRepository;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class PostCommentControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    private Post savedPost;

    @BeforeEach
    void setUp() {
        postCommentRepository.deleteAll();
        postRepository.deleteAll();

        List<PostComment> postCommentList = new ArrayList<>();
        postCommentList.add(new PostComment().setReview("First PostComment").setCreatedOn(LocalDateTime.now()));
        postCommentList.add(new PostComment().setReview("Second PostComment").setCreatedOn(LocalDateTime.now()));
        postCommentList.add(new PostComment().setReview("Third PostComment").setCreatedOn(LocalDateTime.now()));

        Post post = getPost("First Post", "First Content", null);
        postCommentList.forEach(post::addComment);
        savedPost = postRepository.save(post);
    }

    @Test
    void shouldFetchAllPostComments() throws Exception {
        this.mockMvc
                .perform(get("/api/posts/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(savedPost.getComments().size())))
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
        PostComment postComment = savedPost.getComments().getFirst();
        Long postCommentId = postComment.getId();

        this.mockMvc
                .perform(get("/api/posts/comments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postComment.getId()), Long.class))
                .andExpect(jsonPath("$.review", is(postComment.getReview())));
    }

    @Test
    void shouldCreateNewPostComment() throws Exception {
        CreatePostCommentRequest createPostCommentRequest =
                new CreatePostCommentRequest("New PostComment", savedPost.getId());
        this.mockMvc
                .perform(post("/api/posts/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createPostCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.review", is(createPostCommentRequest.review())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostCommentWithoutReview() throws Exception {
        CreatePostCommentRequest postCommentRequest = new CreatePostCommentRequest(null, -99L);

        this.mockMvc
                .perform(post("/api/posts/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts/comments")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("postId")))
                .andExpect(jsonPath("$.violations[0].message", is("PostId should be positive")))
                .andExpect(jsonPath("$.violations[1].field", is("review")))
                .andExpect(jsonPath("$.violations[1].message", is("Review cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePostComment() throws Exception {
        Long postCommentId = savedPost.getComments().getFirst().getId();
        PostCommentRequest postCommentRequest = new PostCommentRequest("Updated PostComment");

        this.mockMvc
                .perform(put("/api/posts/comments/{id}", postCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postCommentId), Long.class))
                .andExpect(jsonPath("$.review", is(postCommentRequest.review())));
    }

    @Test
    void shouldDeletePostComment() throws Exception {
        PostComment postComment = savedPost.getComments().getFirst();

        this.mockMvc
                .perform(delete("/api/posts/comments/{id}", postComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postComment.getId()), Long.class))
                .andExpect(jsonPath("$.review", is(postComment.getReview())));
    }
}
