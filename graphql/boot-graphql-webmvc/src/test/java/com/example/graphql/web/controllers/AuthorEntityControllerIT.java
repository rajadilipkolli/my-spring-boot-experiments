package com.example.graphql.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.model.request.AuthorRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthorEntityControllerIT extends AbstractIntegrationTest {

    private List<AuthorEntity> authorEntityList = null;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();

        authorEntityList = new ArrayList<>();
        authorEntityList.add(new AuthorEntity()
                .setFirstName("First Author")
                .setLastName("lastName")
                .setEmail("junit1@email.com")
                .setMobile(9848022338L));
        authorEntityList.add(new AuthorEntity()
                .setFirstName("Second Author")
                .setLastName("lastName")
                .setEmail("junit2@email.com")
                .setMobile(9848022339L));
        authorEntityList.add(new AuthorEntity()
                .setFirstName("Third Author")
                .setLastName("lastName")
                .setEmail("junit3@email.com")
                .setMobile(9848022340L));
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
        AuthorEntity authorEntity = authorEntityList.getFirst();
        Long authorId = authorEntity.getId();

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorEntity.getFirstName())))
                .andExpect(jsonPath("$.middleName", is(authorEntity.getMiddleName())))
                .andExpect(jsonPath("$.lastName", is(authorEntity.getLastName())));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        AuthorRequest authorRequest =
                new AuthorRequest("New Author", "middleName", "lastName", 9848022338L, "junit4@email.com");

        this.mockMvc
                .perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(authorRequest.firstName())))
                .andExpect(jsonPath("$.middleName", is(authorRequest.middleName())))
                .andExpect(jsonPath("$.lastName", is(authorRequest.lastName())))
                .andExpect(jsonPath("$.registeredAt", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewAuthorWithoutValidData() throws Exception {
        AuthorRequest authorRequest = new AuthorRequest(null, null, null, null, null);

        this.mockMvc
                .perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/validation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/authors")))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("email")))
                .andExpect(jsonPath("$.violations[0].message", is("Email Cant be Blank")))
                .andExpect(jsonPath("$.violations[1].field", is("firstName")))
                .andExpect(jsonPath("$.violations[1].message", is("FirstName Cant be Blank")))
                .andExpect(jsonPath("$.violations[2].field", is("lastName")))
                .andExpect(jsonPath("$.violations[2].message", is("LastName Cant be Blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        AuthorEntity authorEntity = authorEntityList.getFirst();
        authorEntity.setFirstName("Updated Author");

        this.mockMvc
                .perform(put("/api/authors/{id}", authorEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(authorEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorEntity.getFirstName())))
                .andExpect(jsonPath("$.middleName", is(authorEntity.getMiddleName())))
                .andExpect(jsonPath("$.lastName", is(authorEntity.getLastName())));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        AuthorEntity authorEntity = authorEntityList.getFirst();

        this.mockMvc.perform(delete("/api/authors/{id}", authorEntity.getId())).andExpect(status().isAccepted());

        // Verify entity was actually deleted
        assertThat(authorRepository.findById(authorEntity.getId())).isEmpty();
    }
}
