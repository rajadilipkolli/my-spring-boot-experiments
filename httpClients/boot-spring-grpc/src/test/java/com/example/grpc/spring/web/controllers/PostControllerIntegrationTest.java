package com.example.grpc.spring.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.grpc.spring.common.AbstractIntegrationTest;
import com.example.grpc.spring.model.PostDto;
import com.example.grpc.spring.proto.CreatePostRequest;
import com.example.grpc.spring.proto.DeletePostRequest;
import com.example.grpc.spring.proto.DeletePostResponse;
import com.example.grpc.spring.proto.GetPostRequest;
import com.example.grpc.spring.proto.ListPostsRequest;
import com.example.grpc.spring.proto.Post;
import com.example.grpc.spring.proto.UpdatePostRequest;
import com.jayway.jsonpath.JsonPath;
import io.grpc.StatusRuntimeException;
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
                .andExpect(status().isNotFound()); // gRPC translates NOT_FOUND to 404 now
    }

    @Test
    void shouldPerformEndToEndPostCrudFlowUsingClient() {
        // 1. Create a Post
        Post post =
                postServiceBlockingStub.createPost(
                        CreatePostRequest.newBuilder()
                                .setTitle("Test Title1")
                                .setContent("Test Content1")
                                .build());
        assertThat(post.getTitle()).isEqualTo("Test Title1");
        assertThat(post.getContent()).isEqualTo("Test Content1");
        Long postId = post.getId();
        assertThat(postId).isNotNull();

        // 2. Get the Post
        post = postServiceBlockingStub.getPost(GetPostRequest.newBuilder().setId(postId).build());
        assertThat(post.getTitle()).isEqualTo("Test Title1");
        assertThat(post.getContent()).isEqualTo("Test Content1");
        assertThat(post.getId()).isEqualTo(postId);

        // 3. Update the Post
        post =
                postServiceBlockingStub.updatePost(
                        UpdatePostRequest.newBuilder()
                                .setId(postId)
                                .setTitle("Updated Title")
                                .setContent("Updated Content")
                                .build());
        assertThat(post.getTitle()).isEqualTo("Updated Title");
        assertThat(post.getContent()).isEqualTo("Updated Content");
        assertThat(post.getId()).isEqualTo(postId);

        // 4. List Posts
        postServiceBlockingStub
                .listPosts(ListPostsRequest.newBuilder().build())
                .getPostsList()
                .stream()
                .filter(p -> p.getId() == postId)
                .findFirst()
                .ifPresent(p -> assertThat(p.getTitle()).isEqualTo("Updated Title"));

        // 5. Delete the Post
        DeletePostResponse deletePostResponse =
                postServiceBlockingStub.deletePost(
                        DeletePostRequest.newBuilder().setId(postId).build());
        assertThat(deletePostResponse.getSuccess()).isTrue();

        // 6. Verify Deletion
        assertThatThrownBy(
                        () ->
                                postServiceBlockingStub.getPost(
                                        GetPostRequest.newBuilder().setId(postId).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("NOT_FOUND");
    }
}
