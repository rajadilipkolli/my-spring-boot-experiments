package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.model.request.PostDetailsRequest;
import com.example.graphql.model.response.PostDetailsResponse;
import com.example.graphql.services.PostDetailsService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PostDetailsController.class)
@ActiveProfiles(PROFILE_TEST)
class PostDetailsEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostDetailsService postDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<PostDetailsEntity> postDetailsList;

    @BeforeEach
    void setUp() {
        this.postDetailsList = new ArrayList<>();
        this.postDetailsList.add(new PostDetailsEntity().setId(1L).setCreatedBy("Junit1"));
    }

    @Test
    void shouldFetchAllPostDetails() throws Exception {
        given(postDetailsService.findAllPostDetails()).willReturn(List.of(getPostDetails()));

        this.mockMvc
                .perform(get("/api/postdetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postDetailsList.size())));
    }

    @Test
    void shouldFindPostDetailsById() throws Exception {
        Long postDetailsId = 10L;

        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.of(getPostDetails()));

        this.mockMvc
                .perform(get("/api/postdetails/{id}", postDetailsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is("junit")));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingPostDetails() throws Exception {
        Long postDetailsId = 1L;
        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/postdetails/{id}", postDetailsId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePostDetails() throws Exception {
        Long postDetailsId = 1L;
        PostDetailsEntity postDetails =
                new PostDetailsEntity().setId(postDetailsId).setCreatedBy("updated");
        PostDetailsRequest postDetailsRequest = new PostDetailsRequest("junitDetailsKey", "junitCreatedBy");
        given(postDetailsService.findDetailsById(postDetailsId)).willReturn(Optional.of(postDetails));
        given(postDetailsService.updatePostDetails(postDetails, postDetailsRequest))
                .willReturn(Optional.of(getPostDetails()));

        this.mockMvc
                .perform(put("/api/postdetails/{id}", postDetails.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDetailsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detailsKey", is("junitDetailsKey")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingPostDetails() throws Exception {
        Long postDetailsId = 1L;
        given(postDetailsService.findPostDetailsById(postDetailsId)).willReturn(Optional.empty());
        PostDetailsEntity postDetails =
                new PostDetailsEntity().setId(postDetailsId).setCreatedBy("Junit1");

        this.mockMvc
                .perform(put("/api/postdetails/{id}", postDetailsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDetails)))
                .andExpect(status().isNotFound());
    }

    private PostDetailsResponse getPostDetails() {
        return new PostDetailsResponse("junitDetailsKey", LocalDateTime.now(), "junit");
    }
}
