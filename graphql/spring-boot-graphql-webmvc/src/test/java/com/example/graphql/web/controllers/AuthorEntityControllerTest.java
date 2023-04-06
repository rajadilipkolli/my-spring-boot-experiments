package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.services.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = AuthorController.class)
@ActiveProfiles(PROFILE_TEST)
class AuthorEntityControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AuthorService authorService;

    @Autowired private ObjectMapper objectMapper;

    private List<AuthorResponse> authorEntityList;

    @BeforeEach
    void setUp() {
        this.authorEntityList = new ArrayList<>();
        this.authorEntityList.add(
                new AuthorResponse(
                        1L,
                        "First Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit1@email.com",
                        LocalDateTime.now()));
        this.authorEntityList.add(
                new AuthorResponse(
                        2L,
                        "Second Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit2@email.com",
                        LocalDateTime.now()));
        this.authorEntityList.add(
                new AuthorResponse(
                        3L,
                        "Third Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit3@email.com",
                        LocalDateTime.now()));
    }

    @Test
    void shouldFetchAllAuthors() throws Exception {

        given(authorService.findAllAuthors()).willReturn(this.authorEntityList);

        this.mockMvc
                .perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(authorEntityList.size())));
    }

    @Test
    void shouldFindAuthorById() throws Exception {
        Long authorId = 1L;
        AuthorEntity authorEntity =
                AuthorEntity.builder()
                        .id(authorId)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();
        AuthorResponse authorResponse =
                new AuthorResponse(
                        1L,
                        "First Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit1@email.com",
                        LocalDateTime.now());
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(authorResponse));

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorEntity.getFirstName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingAuthor() throws Exception {
        Long authorId = 1L;
        given(authorService.findAuthorById(authorId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/authors/{id}", authorId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {

        AuthorRequest authorRequest =
                new AuthorRequest(
                        "First Author", "middleName", "lastName", 9848022338L, "junit1@email.com");

        AuthorResponse authorResponse =
                new AuthorResponse(
                        1L,
                        "First Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit1@email.com",
                        LocalDateTime.now());

        given(authorService.saveAuthor(authorRequest)).willReturn(authorResponse);

        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registeredAt", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(authorRequest.firstName())));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        Long authorId = 1L;
        AuthorEntity authorEntity =
                AuthorEntity.builder()
                        .id(authorId)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();

        AuthorRequest authorRequest =
                new AuthorRequest(
                        "Updated Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit1@email.com");

        AuthorResponse authorResponse =
                new AuthorResponse(
                        1L,
                        "Updated Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit1@email.com",
                        LocalDateTime.now());
        given(authorService.updateAuthor(authorRequest, authorEntity.getId()))
                .willReturn(Optional.of(authorResponse));

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", authorEntity.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorRequest.firstName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAuthor() throws Exception {
        Long authorId = 100L;
        AuthorRequest authorRequest =
                new AuthorRequest(
                        "First Author", "middleName", "lastName", 9848022338L, "junit4@email.com");

        given(authorService.updateAuthor(authorRequest, authorId)).willReturn(Optional.empty());
        this.mockMvc
                .perform(
                        put("/api/authors/{id}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Long authorId = 1L;
        AuthorResponse authorResponse =
                new AuthorResponse(
                        1L,
                        "First Author",
                        "middleName",
                        "lastName",
                        9848022338L,
                        "junit1@email.com",
                        LocalDateTime.now());
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(authorResponse));
        doNothing().when(authorService).deleteAuthorById(authorId);

        this.mockMvc
                .perform(delete("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorResponse.firstName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingAuthor() throws Exception {
        Long authorId = 1L;
        given(authorService.findAuthorById(authorId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/authors/{id}", authorId))
                .andExpect(status().isNotFound());
    }
}
