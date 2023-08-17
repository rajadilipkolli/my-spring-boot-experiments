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
import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.repositories.AuthorRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AuthorEntityControllerIT extends AbstractIntegrationTest {

    @Autowired private AuthorRepository authorRepository;

    private List<AuthorEntity> authorEntityList = null;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();

        authorEntityList = new ArrayList<>();
        authorEntityList.add(
                AuthorEntity.builder()
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .mobile(9848022338L)
                        .build());
        authorEntityList.add(
                AuthorEntity.builder()
                        .firstName("Second Author")
                        .lastName("lastName")
                        .email("junit2@email.com")
                        .mobile(9848022339L)
                        .build());
        authorEntityList.add(
                AuthorEntity.builder()
                        .firstName("Third Author")
                        .lastName("lastName")
                        .email("junit3@email.com")
                        .mobile(9848022340L)
                        .build());
        authorEntityList = authorRepository.saveAll(authorEntityList);
    }

    @Test
    void shouldFetchAllAuthors() throws Exception {
        this.mockMvc
                .perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(authorEntityList.size())));
    }

    @Test
    void shouldFindAuthorById() throws Exception {
        AuthorEntity authorEntity = authorEntityList.get(0);
        Long authorId = authorEntity.getId();

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorEntity.getFirstName())));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        AuthorRequest authorRequest =
                new AuthorRequest(
                        "New Author", "middleName", "lastName", 9848022338L, "junit4@email.com");

        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(authorRequest.firstName())))
                .andExpect(jsonPath("$.registeredAt", notNullValue()));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        AuthorEntity authorEntity = authorEntityList.get(0);
        authorEntity.setFirstName("Updated Author");

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", authorEntity.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authorEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorEntity.getFirstName())));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        AuthorEntity authorEntity = authorEntityList.get(0);

        this.mockMvc
                .perform(delete("/api/authors/{id}", authorEntity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorEntity.getFirstName())));
    }
}
