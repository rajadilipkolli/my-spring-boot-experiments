package com.example.graphql.web.controllers;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.model.request.AuthorRequest;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.services.AuthorService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthorController.class)
@ActiveProfiles(PROFILE_TEST)
class AuthorEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<AuthorResponse> authorEntityList;

    @BeforeEach
    void setUp() {
        this.authorEntityList = new ArrayList<>();
        this.authorEntityList.add(new AuthorResponse(
                1L, "First Author", "middleName", "lastName", 9848022338L, "junit1@email.com", LocalDateTime.now()));
        this.authorEntityList.add(new AuthorResponse(
                2L, "Second Author", "middleName", "lastName", 9848022338L, "junit2@email.com", LocalDateTime.now()));
        this.authorEntityList.add(new AuthorResponse(
                3L, "Third Author", "middleName", "lastName", 9848022338L, "junit3@email.com", LocalDateTime.now()));
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
        AuthorEntity authorEntity = new AuthorEntity()
                .setId(authorId)
                .setFirstName("First Author")
                .setLastName("lastName")
                .setEmail("junit1@email.com");
        AuthorResponse authorResponse = new AuthorResponse(
                1L, "First Author", "middleName", "lastName", 9848022338L, "junit1@email.com", LocalDateTime.now());
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

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Author: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/authors/1")));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {

        AuthorRequest authorRequest =
                new AuthorRequest("First Author", "middleName", "lastName", 9848022338L, "junit1@email.com");

        AuthorResponse authorResponse = new AuthorResponse(
                1L, "First Author", "middleName", "lastName", 9848022338L, "junit1@email.com", LocalDateTime.now());

        given(authorService.saveAuthor(authorRequest)).willReturn(authorResponse);

        this.mockMvc
                .perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registeredAt", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(authorRequest.firstName())));
    }

    @Test
    void shouldReturn400WhenCreateNewAuthorWithoutValidData() throws Exception {
        AuthorRequest authorRequest = new AuthorRequest(null, null, null, null, null);

        this.mockMvc
                .perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
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
        Long authorId = 1L;
        AuthorEntity authorEntity = new AuthorEntity()
                .setId(authorId)
                .setFirstName("First Author")
                .setLastName("lastName")
                .setEmail("junit1@email.com");

        AuthorRequest authorRequest =
                new AuthorRequest("Updated Author", "middleName", "lastName", 9848022338L, "junit1@email.com");

        AuthorResponse authorResponse = new AuthorResponse(
                1L, "Updated Author", "middleName", "lastName", 9848022338L, "junit1@email.com", LocalDateTime.now());
        given(authorService.updateAuthor(authorRequest, authorEntity.getId())).willReturn(Optional.of(authorResponse));

        this.mockMvc
                .perform(put("/api/authors/{id}", authorEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(authorRequest.firstName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAuthor() throws Exception {
        Long authorId = 100L;
        AuthorRequest authorRequest =
                new AuthorRequest("First Author", "middleName", "lastName", 9848022338L, "junit4@email.com");

        given(authorService.updateAuthor(authorRequest, authorId)).willReturn(Optional.empty());
        this.mockMvc
                .perform(put("/api/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Author: 100 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/authors/100")));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Long authorId = 1L;
        AuthorResponse authorResponse = new AuthorResponse(
                1L, "First Author", "middleName", "lastName", 9848022338L, "junit1@email.com", LocalDateTime.now());
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
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.graphql-webmvc.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Author: 1 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/authors/1")));
    }
}
