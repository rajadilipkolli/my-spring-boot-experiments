package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        this.postDetailsList.add(PostDetails.builder().id(1L).createdBy("Junit1").build());
        this.postDetailsList.add(PostDetails.builder().id(2L).createdBy("Junit2").build());
        this.postDetailsList.add(PostDetails.builder().id(3L).createdBy("Junit3").build());

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
        PostDetails postDetails =
                PostDetails.builder().id(postDetailsId).createdBy("Junit1").build();
        given(postDetailsService.findPostDetailsById(postDetailsId))
                .willReturn(Optional.of(postDetails));

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
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

        PostDetails postDetails = PostDetails.builder().id(1L).createdBy("Junit1").build();
        this.mockMvc
                .perform(
                        post("/api/postdetails")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    void shouldUpdatePostDetails() throws Exception {
        Long postDetailsId = 1L;
        PostDetails postDetails =
                PostDetails.builder().id(postDetailsId).createdBy("updated").build();
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
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPostDetails() throws Exception {
        Long postDetailsId = 1L;
        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.empty());
        PostDetails postDetails =
                PostDetails.builder().id(postDetailsId).createdBy("Junit1").build();

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
        PostDetails postDetails =
                PostDetails.builder().id(postDetailsId).createdBy("Junit1").build();
        given(postDetailsService.findPostDetailsById(postDetailsId))
                .willReturn(Optional.of(postDetails));
        doNothing().when(postDetailsService).deletePostDetailsById(postDetails.getId());

        this.mockMvc
                .perform(delete("/api/postdetails/{id}", postDetails.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is(postDetails.getCreatedBy())));
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
