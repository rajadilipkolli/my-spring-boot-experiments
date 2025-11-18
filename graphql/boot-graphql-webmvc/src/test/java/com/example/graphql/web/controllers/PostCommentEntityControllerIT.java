package com.example.graphql.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.model.request.PostCommentRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PostCommentEntityControllerIT extends AbstractIntegrationTest {

    private PostEntity postEntity;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        postEntity = new PostEntity().setContent("First Post").setTitle("First Title");
        PostDetailsEntity postDetailsEntity = new PostDetailsEntity().setDetailsKey("First Details");
        postEntity.setDetails(postDetailsEntity);

        List<PostCommentEntity> postCommentEntityList = new ArrayList<>();
        postCommentEntityList.add(
                new PostCommentEntity().setTitle("First PostComment").setPublished(true));
        postCommentEntityList.add(
                new PostCommentEntity().setTitle("Second PostComment").setPublished(false));
        postCommentEntityList.add(new PostCommentEntity().setTitle("Third PostComment"));
        postCommentEntityList.forEach(postCommentEntity -> postEntity.addComment(postCommentEntity));
        postRepository.save(postEntity);
    }

    @Test
    void shouldFetchAllPostComments() throws Exception {
        this.mockMvc
                .perform(get("/api/post/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(postEntity.getComments().size())));
    }

    @Test
    void shouldFindPostCommentById() throws Exception {
        PostCommentEntity postCommentEntity = postEntity.getComments().getFirst();
        Long postCommentId = postCommentEntity.getId();

        this.mockMvc
                .perform(get("/api/post/comments/{id}", postCommentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is(postCommentEntity.getTitle())));
    }

    @Test
    void shouldCreateNewPostComment() throws Exception {
        PostCommentRequest postCommentRequest =
                new PostCommentRequest("First PostComment", "First Content", String.valueOf(postEntity.getId()), true);

        this.mockMvc
                .perform(post("/api/post/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postId", is(postEntity.getId()), Long.class))
                .andExpect(jsonPath("$.commentId", notNullValue(Long.class)))
                .andExpect(jsonPath("$.title", is(postCommentRequest.title())))
                .andExpect(jsonPath("$.content", is(postCommentRequest.content())))
                .andExpect(jsonPath("$.published", is(postCommentRequest.published())))
                .andExpect(jsonPath("$.publishedAt", notNullValue(LocalDateTime.class)));
    }

    @Test
    void shouldUpdatePostComment() throws Exception {
        PostCommentEntity postCommentEntity = postEntity.getComments().getFirst();

        PostCommentRequest postCommentRequest = new PostCommentRequest(
                "Updated PostComment",
                postCommentEntity.getContent(),
                String.valueOf(postEntity.getId()),
                postCommentEntity.isPublished());
        this.mockMvc
                .perform(put("/api/post/comments/{id}", postCommentEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(postCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Updated PostComment")));
    }

    @Test
    void shouldDeletePostComment() throws Exception {
        PostCommentEntity postCommentEntity = postEntity.getComments().getFirst();

        this.mockMvc
                .perform(delete("/api/post/comments/{id}", postCommentEntity.getId()))
                .andExpect(status().isAccepted());

        // Verify entity was actually deleted
        assertThat(postCommentRepository.findById(postCommentEntity.getId())).isEmpty();
    }
}
