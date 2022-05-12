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

import com.example.graphql.entities.Author;
import com.example.graphql.services.AuthorService;
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

@WebMvcTest(controllers = AuthorController.class)
@ActiveProfiles(PROFILE_TEST)
class AuthorControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AuthorService authorService;

    @Autowired private ObjectMapper objectMapper;

    private List<Author> authorList;

    @BeforeEach
    void setUp() {
        this.authorList = new ArrayList<>();
        this.authorList.add(new Author(1L, "text 1", "junit1@email.com"));
        this.authorList.add(new Author(2L, "text 2", "junit2@email.com"));
        this.authorList.add(new Author(3L, "text 3", "junit3@email.com"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllAuthors() throws Exception {
        given(authorService.findAllAuthors()).willReturn(this.authorList);

        this.mockMvc
                .perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(authorList.size())));
    }

    @Test
    void shouldFindAuthorById() throws Exception {
        Long authorId = 1L;
        Author author = new Author(authorId, "text 1", "junit1@email.com");
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(author));

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(author.getName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingAuthor() throws Exception {
        Long authorId = 1L;
        given(authorService.findAuthorById(authorId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/authors/{id}", authorId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        given(authorService.saveAuthor(any(Author.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Author author = new Author(1L, "some text", "junit1@email.com");
        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(author.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewAuthorWithoutText() throws Exception {
        Author author = new Author(null, null, null);

        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
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
    void shouldUpdateAuthor() throws Exception {
        Long authorId = 1L;
        Author author = new Author(authorId, "Updated text", "junit1@email.com");
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(author));
        given(authorService.saveAuthor(any(Author.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", author.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(author.getName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAuthor() throws Exception {
        Long authorId = 1L;
        given(authorService.findAuthorById(authorId)).willReturn(Optional.empty());
        Author author = new Author(authorId, "Updated text", "junit1@email.com");

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Long authorId = 1L;
        Author author = new Author(authorId, "Some text", "junit1@email.com");
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(author));
        doNothing().when(authorService).deleteAuthorById(author.getId());

        this.mockMvc
                .perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(author.getName())));
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
