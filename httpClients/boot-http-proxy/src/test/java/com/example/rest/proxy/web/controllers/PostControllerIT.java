package com.example.rest.proxy.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.example.rest.proxy.common.AbstractIntegrationTest;
import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.repositories.PostRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PostControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    private List<Post> postList = null;

    @BeforeEach
    void setUp() {
        postRepository.deleteAllInBatch();

        postList = new ArrayList<>();
        postList.add(new Post()
                .setTitle("First Post")
                .setUserId(1L)
                .setBody("First Body")
                .setPostComments(new ArrayList<>()));
        postList.add(new Post()
                .setTitle("Second Post")
                .setUserId(1L)
                .setBody("Second Body")
                .setPostComments(new ArrayList<>()));
        postList.add(new Post()
                .setTitle("Third Post")
                .setUserId(1L)
                .setBody("ThirdBody")
                .setPostComments(new ArrayList<>()));
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
                .andExpect(jsonPath("$.postId", is(post.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.userId", is(post.getUserId()), Long.class))
                .andExpect(jsonPath("$.body", is(post.getBody())));
    }

    @Test
    void shouldFindPostByIdBySavingData() throws Exception {
        long count = this.postRepository.count();

        this.mockMvc
                .perform(get("/api/posts/{id}", 75))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", notNullValue()))
                .andExpect(jsonPath("$.title", is("dignissimos eum dolor ut enim et delectus in")))
                .andExpect(jsonPath("$.userId", is(8)))
                .andExpect(
                        jsonPath(
                                "$.body",
                                is(
                                        "commodi non non omnis et voluptas sit\nautem aut nobis magnam et sapiente voluptatem\net laborum repellat qui delectus facilis temporibus\nrerum amet et nemo voluptate expedita adipisci error dolorem")));

        assertThat(this.postRepository.count()).isEqualTo(count + 1);
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        Post post = new Post().setTitle("New Post").setUserId(1L).setBody("First Body");
        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId", notNullValue()))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.userId", is(post.getUserId()), Long.class))
                .andExpect(jsonPath("$.body", is(post.getBody())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitleAndBody() throws Exception {
        Post post = new Post().setUserId(0L);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.http-proxy.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/posts")))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("body")))
                .andExpect(jsonPath("$.violations[0].message", is("Body cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("title")))
                .andExpect(jsonPath("$.violations[1].message", is("Title cannot be empty")))
                .andExpect(jsonPath("$.violations[2].field", is("userId")))
                .andExpect(jsonPath("$.violations[2].message", is("UserId Should be positive Number")))
                .andReturn();
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Post post = postList.getFirst();
        post.setTitle("Updated Post Title");

        this.mockMvc
                .perform(put("/api/posts/{id}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(post.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.userId", is(post.getUserId()), Long.class))
                .andExpect(jsonPath("$.body", is(post.getBody())));
    }

    @Test
    void shouldDeletePost() throws Exception {
        Post post = postList.getFirst();

        this.mockMvc
                .perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(post.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(post.getTitle())))
                .andExpect(jsonPath("$.userId", is(post.getUserId()), Long.class))
                .andExpect(jsonPath("$.body", is(post.getBody())));
    }
}
