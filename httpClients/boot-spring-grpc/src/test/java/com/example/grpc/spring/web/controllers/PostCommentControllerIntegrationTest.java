package com.example.grpc.spring.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.grpc.spring.common.AbstractIntegrationTest;
import com.example.grpc.spring.model.PostCommentDto;
import com.example.grpc.spring.model.PostDto;
import com.example.grpc.spring.proto.AddCommentRequest;
import com.example.grpc.spring.proto.CreatePostRequest;
import com.example.grpc.spring.proto.DeleteCommentRequest;
import com.example.grpc.spring.proto.DeleteCommentResponse;
import com.example.grpc.spring.proto.GetCommentRequest;
import com.example.grpc.spring.proto.ListCommentsRequest;
import com.example.grpc.spring.proto.Post;
import com.example.grpc.spring.proto.PostComment;
import com.example.grpc.spring.proto.UpdateCommentRequest;
import com.jayway.jsonpath.JsonPath;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class PostCommentControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldPerformEndToEndPostCommentCrudFlow() throws Exception {
        // 1. Create a Post first (Parent)
        PostDto createPostRequest = new PostDto(null, "Comment Test Title", "Content");
        MvcResult postResult =
                mockMvc.perform(
                                post("/api/posts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(createPostRequest)))
                        .andExpect(status().isOk())
                        .andReturn();

        Long postId =
                ((Number) JsonPath.read(postResult.getResponse().getContentAsString(), "$.id"))
                        .longValue();

        // 2. Add a Comment
        PostCommentDto addCommentRequest = new PostCommentDto(null, postId, "This is a review");
        MvcResult commentResult =
                mockMvc.perform(
                                post("/api/posts/{postId}/comments", postId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonMapper.writeValueAsString(addCommentRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.review").value("This is a review"))
                        .andReturn();

        Long commentId =
                ((Number) JsonPath.read(commentResult.getResponse().getContentAsString(), "$.id"))
                        .longValue();

        // 3. Get the Comment
        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").value("This is a review"));

        // 4. Update the Comment
        PostCommentDto updateCommentRequest = new PostCommentDto(null, postId, "Updated review");
        mockMvc.perform(
                        put("/api/posts/{postId}/comments/{id}", postId, commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(updateCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").value("Updated review"));

        // 5. List Comments
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].review").value("Updated review"));

        // 6. Delete the Comment
        mockMvc.perform(delete("/api/posts/{postId}/comments/{id}", postId, commentId))
                .andExpect(status().isNoContent());

        // 7. Verify Deletion
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldPerformEndToEndPostCommentCrudFlowUsingStub() {
        // 1. Create a Post first (Parent)
        Post post =
                postServiceBlockingStub.createPost(
                        CreatePostRequest.newBuilder()
                                .setTitle("Test Title2")
                                .setContent("Test Content2")
                                .build());
        assertThat(post.getTitle()).isEqualTo("Test Title2");
        assertThat(post.getContent()).isEqualTo("Test Content2");
        Long postId = post.getId();
        assertThat(postId).isNotNull();

        // 2. Add a Comment
        PostComment postComment =
                postCommentServiceBlockingStub.addComment(
                        AddCommentRequest.newBuilder()
                                .setPostId(postId)
                                .setReview("This is a review")
                                .build());
        assertThat(postComment.getPostId()).isEqualTo(postId);
        assertThat(postComment.getReview()).isEqualTo("This is a review");
        Long commentId = postComment.getId();
        assertThat(commentId).isNotNull();

        // 3. Get the Comment
        postComment =
                postCommentServiceBlockingStub.getComment(
                        GetCommentRequest.newBuilder().setPostId(postId).setId(commentId).build());
        assertThat(postComment.getPostId()).isEqualTo(postId);
        assertThat(postComment.getReview()).isEqualTo("This is a review");
        assertThat(postComment.getId()).isEqualTo(commentId);

        // 4. Update the Comment
        postComment =
                postCommentServiceBlockingStub.updateComment(
                        UpdateCommentRequest.newBuilder()
                                .setPostId(postId)
                                .setId(commentId)
                                .setReview("Updated review")
                                .build());
        assertThat(postComment.getPostId()).isEqualTo(postId);
        assertThat(postComment.getReview()).isEqualTo("Updated review");
        assertThat(postComment.getId()).isEqualTo(commentId);

        // 5. List Comments
        assertThat(
                        postCommentServiceBlockingStub
                                .listComments(
                                        ListCommentsRequest.newBuilder().setPostId(postId).build())
                                .getCommentsList())
                .anySatisfy(
                        c -> {
                            assertThat(c.getId()).isEqualTo(commentId);
                            assertThat(c.getReview()).isEqualTo("Updated review");
                        });

        // 6. Delete the Comment
        DeleteCommentResponse deleteCommentResponse =
                postCommentServiceBlockingStub.deleteComment(
                        DeleteCommentRequest.newBuilder()
                                .setPostId(postId)
                                .setId(commentId)
                                .build());
        assertThat(deleteCommentResponse.getSuccess()).isTrue();

        // 7. Verify Deletion
        assertThatThrownBy(
                        () ->
                                postCommentServiceBlockingStub.getComment(
                                        GetCommentRequest.newBuilder()
                                                .setPostId(postId)
                                                .setId(commentId)
                                                .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("NOT_FOUND");
    }
}
