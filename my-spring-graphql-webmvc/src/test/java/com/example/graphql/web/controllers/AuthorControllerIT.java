package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.entities.Author;
import com.example.graphql.repositories.AuthorRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AuthorControllerIT extends AbstractIntegrationTest {

    @Autowired private AuthorRepository authorRepository;

    private List<Author> authorList = null;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();

        authorList = new ArrayList<>();
        authorList.add(new Author(1L, "First Author"));
        authorList.add(new Author(2L, "Second Author"));
        authorList.add(new Author(3L, "Third Author"));
        authorList = authorRepository.saveAll(authorList);
    }

    @Test
    void shouldFetchAllAuthors() throws Exception {
        this.mockMvc
                .perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(authorList.size())));
    }

    @Test
    void shouldFindAuthorById() throws Exception {
        Author author = authorList.get(0);
        Long authorId = author.getId();

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(author.getText())));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        Author author = new Author(null, "New Author");
        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(author.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewAuthorWithoutText() throws Exception {
        Author author = new Author(null, null);

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
        Author author = authorList.get(0);
        author.setText("Updated Author");

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", author.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(author.getText())));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Author author = authorList.get(0);

        this.mockMvc
                .perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(author.getText())));
    }
}
