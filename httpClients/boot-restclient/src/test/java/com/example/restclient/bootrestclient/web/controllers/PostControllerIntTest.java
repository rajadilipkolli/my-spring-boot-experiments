package com.example.restclient.bootrestclient.web.controllers;

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

import com.example.restclient.bootrestclient.model.response.PostDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldFindPostById() throws Exception {

        this.mockMvc
                .perform(get("/api/posts/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath(
                        "$.title", is("sunt aut facere repellat provident occaecati excepturi optio reprehenderit")))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(
                        jsonPath(
                                "$.body",
                                is(
                                        "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto")));
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        PostDto postDto = new PostDto(1L, null, "First Title", "First Body");
        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is(postDto.title())))
                .andExpect(jsonPath("$.body", is(postDto.body())))
                .andExpect(jsonPath("$.userId", is(postDto.userId()), Long.class));
    }

    @Test
    void shouldReturn400WhenCreateNewPostWithoutTitle() throws Exception {
        PostDto post = new PostDto(1L, null, null, "First Body");

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(post)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.boot-restclient.com/errors/constraint-violation")))
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
        PostDto postDto = new PostDto(1L, 1L, "First Title", "First Body");

        this.mockMvc
                .perform(put("/api/posts/{id}", postDto.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postDto.id()), Long.class))
                .andExpect(jsonPath("$.title", is(postDto.title())))
                .andExpect(jsonPath("$.body", is(postDto.body())))
                .andExpect(jsonPath("$.userId", is(postDto.userId()), Long.class));
    }

    @Test
    void shouldDeletePost() throws Exception {

        String response = this.mockMvc
                .perform(delete("/api/posts/{id}", 50))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(response).isEqualTo("{}");
    }
}
