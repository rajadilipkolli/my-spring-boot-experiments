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

import com.example.grpc.spring.model.PostCommentDto;
import com.example.grpc.spring.services.client.PostCommentClientService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PostCommentController.class)
class PostCommentControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private PostCommentClientService postCommentClientService;

    @Test
    void shouldAddComment() throws Exception {
        PostCommentDto request = new PostCommentDto(null, null, "Great post!");
        PostCommentDto response = new PostCommentDto(1L, 100L, "Great post!");

        given(postCommentClientService.addComment(eq(100L), any(PostCommentDto.class)))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/posts/{postId}/comments", 100L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.review").value("Great post!"));
    }

    @Test
    void shouldGetComment() throws Exception {
        PostCommentDto response = new PostCommentDto(1L, 100L, "Great post!");
        given(postCommentClientService.getComment(100L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", 100L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.review").value("Great post!"));
    }

    @Test
    void shouldUpdateComment() throws Exception {
        PostCommentDto request = new PostCommentDto(null, null, "Updated review");
        PostCommentDto response = new PostCommentDto(1L, 100L, "Updated review");

        given(postCommentClientService.updateComment(eq(100L), eq(1L), any(PostCommentDto.class)))
                .willReturn(response);

        mockMvc.perform(
                        put("/api/posts/{postId}/comments/{id}", 100L, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").value("Updated review"));
    }

    @Test
    void shouldDeleteComment() throws Exception {
        given(postCommentClientService.deleteComment(100L, 1L)).willReturn(true);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{id}", 100L, 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentComment() throws Exception {
        given(postCommentClientService.deleteComment(100L, 1L)).willReturn(false);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{id}", 100L, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListComments() throws Exception {
        given(postCommentClientService.listComments(100L))
                .willReturn(
                        List.of(
                                new PostCommentDto(1L, 100L, "Review 1"),
                                new PostCommentDto(2L, 100L, "Review 2")));

        mockMvc.perform(get("/api/posts/{postId}/comments", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].review").value("Review 1"));
    }
}
