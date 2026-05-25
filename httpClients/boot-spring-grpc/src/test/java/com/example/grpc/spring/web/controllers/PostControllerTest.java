package com.example.grpc.spring.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.grpc.spring.model.PostDto;
import com.example.grpc.spring.services.client.PostClientService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private JsonMapper jsonMapper;

    @MockitoBean private PostClientService postClientService;

    @Test
    void shouldCreatePost() throws Exception {
        PostDto request = new PostDto(null, "Title", "Content");
        PostDto response = new PostDto(1L, "Title", "Content");

        given(postClientService.createPost(any(PostDto.class))).willReturn(response);

        mockMvc.perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void shouldGetPost() throws Exception {
        PostDto response = new PostDto(1L, "Title", "Content");
        given(postClientService.getPost(1L)).willReturn(response);

        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void shouldUpdatePost() throws Exception {
        PostDto request = new PostDto(null, "Updated Title", "Updated Content");
        PostDto response = new PostDto(1L, "Updated Title", "Updated Content");

        given(postClientService.updatePost(eq(1L), any(PostDto.class))).willReturn(response);

        mockMvc.perform(
                        put("/api/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void shouldDeletePost() throws Exception {
        given(postClientService.deletePost(1L)).willReturn(true);

        mockMvc.perform(delete("/api/posts/{id}", 1L)).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPost() throws Exception {
        given(postClientService.deletePost(1L)).willReturn(false);

        mockMvc.perform(delete("/api/posts/{id}", 1L)).andExpect(status().isNotFound());
    }

    @Test
    void shouldListPosts() throws Exception {
        given(postClientService.listPosts())
                .willReturn(
                        List.of(
                                new PostDto(1L, "Title 1", "Content 1"),
                                new PostDto(2L, "Title 2", "Content 2")));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Title 1"));
    }
}
