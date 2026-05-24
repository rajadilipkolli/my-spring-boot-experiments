package com.example.grpc.spring.web.controllers;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.grpc.spring.common.AbstractIntegrationTest;
import com.example.grpc.spring.model.PostDto;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class PostControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldPerformEndToEndPostCrudFlow() throws Exception {
        // 1. Create a Post
        PostDto createRequest = new PostDto(null, "Test Title", "Test Content");
        MvcResult createResult =
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(createRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value("Test Title"))
                        .andExpect(jsonPath("$.content").value("Test Content"))
                        .andReturn();

        Long postId =
                ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.id"))
                        .longValue();

        // 2. Get the Post
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"));

        // 3. Update the Post
        PostDto updateRequest = new PostDto(null, "Updated Title", "Updated Content");
        mockMvc.perform(
                        put("/api/posts/{id}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        // 4. List Posts
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Updated Title")));

        // 5. Delete the Post
        mockMvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isNoContent());

        // 6. Verify Deletion
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isInternalServerError()); // gRPC translates NOT_FOUND to
        // StatusRuntimeException wrapped in 500
        // currently unless explicitly handled
        // globally
    }
}
