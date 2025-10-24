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
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.request.TagRequest;
import com.example.graphql.querydsl.model.request.UpdatePostRequest;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class PostControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    private List<Post> postList = null;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        postList = new ArrayList<>();
        postList.add(getPost("First Post", "First Content", "First Review"));
        postList.add(getPost("Second Post", "Second Content", "Second Review"));
        postList.add(getPost("Third Post", "Third Content", "Third Review"));
        postList = postRepository.saveAll(postList);
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
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
        Post post = postList.getFirst();
        Long postId = post.getId();

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(post.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.content", is(post.getContent())))
                .andExpect(jsonPath("$.createdOn", is("2023-12-31T10:35:45")));
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        CreatePostRequest postRequest = new CreatePostRequest(
                "New Post",
                "New Content",
                "Junit",
                List.of(new PostCommentRequest("First Review"), new PostCommentRequest("Second Review")),
                List.of(new TagRequest("java")));
        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is(postRequest.title())))
                .andExpect(jsonPath("$.content", is(postRequest.content())))
                .andExpect(
                        jsonPath("$.comments.size()", is(postRequest.comments().size())))
                .andExpect(jsonPath("$.comments[0].id", notNullValue()))
                .andExpect(jsonPath("$.comments[0].review", is("First Review")))
                .andExpect(jsonPath("$.comments[1].id", notNullValue()))
                .andExpect(jsonPath("$.comments[1].review", is("Second Review")));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitleAndContent() throws Exception {
        CreatePostRequest createPostRequest =
                new CreatePostRequest(null, null, null, new ArrayList<>(), new ArrayList<>());

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createPostRequest)))
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

    @Test
    void shouldUpdatePost() throws Exception {
        Long postId = postList.getFirst().getId();
        UpdatePostRequest updatePostRequest = new UpdatePostRequest("Updated Post", "New Content");

        this.mockMvc
                .perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updatePostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postId), Long.class))
                .andExpect(jsonPath("$.title", is(updatePostRequest.title())))
                .andExpect(jsonPath("$.content", is(updatePostRequest.content())));
    }

    @Test
    void shouldDeletePost() throws Exception {
        Post post = postList.getFirst();

        this.mockMvc
                .perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(post.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.content", is(post.getContent())))
                .andExpect(jsonPath("$.createdOn", is("2023-12-31T10:35:45")));
    }
}
