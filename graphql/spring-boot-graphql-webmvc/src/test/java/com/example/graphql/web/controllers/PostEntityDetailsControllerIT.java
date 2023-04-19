package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.PostDetailsEntity;
import com.example.graphql.repositories.PostDetailsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

class PostEntityDetailsControllerIT extends AbstractIntegrationTest {

    @Autowired private PostDetailsRepository postDetailsRepository;

    private List<PostDetailsEntity> postDetailsEntityList;

    @BeforeEach
    void setUp() {
        postDetailsRepository.deleteAll();
        postDetailsEntityList = new ArrayList<>();
        postDetailsEntityList.add(PostDetailsEntity.builder().createdBy("Junit1").build());
        postDetailsEntityList.add(PostDetailsEntity.builder().createdBy("Junit2").build());
        postDetailsEntityList.add(PostDetailsEntity.builder().createdBy("Junit3").build());
        postDetailsEntityList = this.postDetailsRepository.findAll();
    }

    @Test
    void shouldFetchAllPostDetails() throws Exception {
        this.mockMvc
                .perform(get("/api/postdetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(postDetailsEntityList.size())));
    }
}
