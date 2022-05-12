package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.PostDetails;
import com.example.graphql.services.PostDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = PostDetailsController.class)
@ActiveProfiles(PROFILE_TEST)
class PostDetailsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PostDetailsService postDetailsService;

    @Autowired private ObjectMapper objectMapper;

    private List<PostDetails> postDetailsList;

    @BeforeEach
    void setUp() {
        this.postDetailsList = new ArrayList<>();
        this.postDetailsList.add(new PostDetails(1L, "text 1"));
        this.postDetailsList.add(new PostDetails(2L, "text 2"));
        this.postDetailsList.add(new PostDetails(3L, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllPostDetailss() throws Exception {
        given(postDetailsService.findAllPostDetailss()).willReturn(this.postDetailsList);

        this.mockMvc
                .perform(get("/api/postdetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postDetailsList.size())));
    }

    @Test
    void shouldFindPostDetailsById() throws Exception {
        Long postDetailsId = 1L;
        PostDetails postDetails = new PostDetails(postDetailsId, "text 1");
        given(postDetailsService.findPostDetailsById(postDetailsId))
                .willReturn(Optional.of(postDetails));

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPostDetails() throws Exception {
        Long postDetailsId = 1L;
        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPostDetails() throws Exception {
        given(postDetailsService.savePostDetails(any(PostDetails.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        PostDetails postDetails = new PostDetails(1L, "some text");
        this.mockMvc
                .perform(
                        post("/api/postdetails")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewPostDetailsWithoutText() throws Exception {
        PostDetails postDetails = new PostDetails(null, null);

        this.mockMvc
                .perform(
                        post("/api/postdetails")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdatePostDetails() throws Exception {
        Long postDetailsId = 1L;
        PostDetails postDetails = new PostDetails(postDetailsId, "Updated text");
        given(postDetailsService.findPostDetailsById(postDetailsId))
                .willReturn(Optional.of(postDetails));
        given(postDetailsService.savePostDetails(any(PostDetails.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/postdetails/{id}", postDetails.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPostDetails() throws Exception {
        Long postDetailsId = 1L;
        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.empty());
        PostDetails postDetails = new PostDetails(postDetailsId, "Updated text");

        this.mockMvc
                .perform(
                        put("/api/postdetails/{id}", postDetailsId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePostDetails() throws Exception {
        Long postDetailsId = 1L;
        PostDetails postDetails = new PostDetails(postDetailsId, "Some text");
        given(postDetailsService.findPostDetailsById(postDetailsId))
                .willReturn(Optional.of(postDetails));
        doNothing().when(postDetailsService).deletePostDetailsById(postDetails.getId());

        this.mockMvc
                .perform(delete("/api/postdetails/{id}", postDetails.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(postDetails.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingPostDetails() throws Exception {
        Long postDetailsId = 1L;
        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isNotFound());
    }
}
