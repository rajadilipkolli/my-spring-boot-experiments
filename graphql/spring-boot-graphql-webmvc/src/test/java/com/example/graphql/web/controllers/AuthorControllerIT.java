package com.example.graphql.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        authorList.add(Author.builder().name("First Author").email("junit1@email.com").build());
        authorList.add(Author.builder().name("Second Author").email("junit2@email.com").build());
        authorList.add(Author.builder().name("Third Author").email("junit3@email.com").build());
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
                .andExpect(jsonPath("$.name", is(author.getName())));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        Author author = Author.builder().name("New Author").email("junit4@email.com").build();
        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(author.getName())));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        Author author = authorList.get(0);
        author.setName("Updated Author");

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", author.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(author.getName())));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Author author = authorList.get(0);

        this.mockMvc
                .perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(author.getName())));
    }
}
