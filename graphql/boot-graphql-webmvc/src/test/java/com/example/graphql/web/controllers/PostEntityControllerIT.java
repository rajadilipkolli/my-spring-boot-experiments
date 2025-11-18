package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.entities.TagEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.request.TagsRequest;
import com.example.graphql.repositories.PostRepository;
import com.example.graphql.repositories.PostTagRepository;
import com.example.graphql.repositories.TagRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PostEntityControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private TagRepository tagRepository;

    private List<PostEntity> postEntityList = null;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        postEntityList = new ArrayList<>();
        PostEntity firstPost = new PostEntity().setContent("First Post");
        firstPost.setDetails(new PostDetailsEntity().setDetailsKey("Junit1"));
        firstPost.addTag(new TagEntity().setTagName("junit"));
        firstPost.addTag(new TagEntity().setTagName("spring"));
        postEntityList.add(firstPost);
        PostEntity secondPost = new PostEntity().setContent("Second Post");
        secondPost.setDetails(new PostDetailsEntity().setDetailsKey("Junit2"));
        postEntityList.add(secondPost);
        PostEntity thirdPost = new PostEntity().setContent("Third Post");
        thirdPost.setDetails(new PostDetailsEntity().setDetailsKey("Junit3"));
        postEntityList.add(thirdPost);
        postEntityList = postRepository.saveAll(postEntityList);
    }

    @AfterEach
    void tearDown() {
        postTagRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void shouldFetchAllPosts() throws Exception {
        this.mockMvc
                .perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postEntityList.size())));
    }

    @Test
    void shouldFindPostById() throws Exception {
        PostEntity postEntity = postEntityList.getFirst();
        Long postId = postEntity.getId();

        this.mockMvc
                .perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is(postEntity.getContent())));
    }

    @Test
    void shouldCreateNewPost() throws Exception {
        NewPostRequest newPostRequest = new NewPostRequest(
                "First Title",
                "New Post",
                "junit1@email.com",
                false,
                new PostDetailsRequest("detailsKey", "JunitCreatedBy"),
                null);

        this.mockMvc
                .perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newPostRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is(newPostRequest.content())))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.title", is(newPostRequest.title())));
    }

    @Test
    void shouldUpdatePost() throws Exception {

        PostEntity postEntityListFirst = postEntityList.getFirst();
        NewPostRequest newPostRequest = new NewPostRequest(
                "First Title",
                "Updated Post",
                "junit1@email.com",
                false,
                new PostDetailsRequest(
                        postEntityListFirst.getDetails().getDetailsKey(),
                        postEntityListFirst.getDetails().getCreatedBy()),
                List.of(
                        new TagsRequest("java", "Programming Language"),
                        new TagsRequest("spring", "Architecture Language")));

        this.mockMvc
                .perform(put("/api/posts/{id}", postEntityListFirst.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(newPostRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(newPostRequest.title())))
                .andExpect(jsonPath("$.content", is(newPostRequest.content())))
                .andExpect(jsonPath("$.published", is(newPostRequest.published())))
                .andExpect(jsonPath("$.tags.size()", is(2)));
    }

    @Test
    void shouldDeletePost() throws Exception {
        PostEntity postEntity = postEntityList.getFirst();

        this.mockMvc.perform(delete("/api/posts/{id}", postEntity.getId())).andExpect(status().isAccepted());
    }
}
