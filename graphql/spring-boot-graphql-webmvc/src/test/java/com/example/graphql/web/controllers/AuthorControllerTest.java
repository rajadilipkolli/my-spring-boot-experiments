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
        this.authorList.add(
                Author.builder()
                        .id(1L)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build());
        this.authorList.add(
                Author.builder()
                        .id(2L)
                        .firstName("Second Author")
                        .lastName("lastName")
                        .email("junit2@email.com")
                        .build());
        this.authorList.add(
                Author.builder()
                        .id(3L)
                        .firstName("Third Author")
                        .lastName("lastName")
                        .email("junit3@email.com")
                        .build());
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
        Author author =
                Author.builder()
                        .id(authorId)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(author));

        this.mockMvc
                .perform(get("/api/authors/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(author.getFirstName())));
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

        Author author =
                Author.builder()
                        .id(1L)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();
        ;
        this.mockMvc
                .perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(author.getFirstName())));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        Long authorId = 1L;
        Author author =
                Author.builder()
                        .id(authorId)
                        .firstName("Updated Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(author));
        given(authorService.saveAuthor(any(Author.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/api/authors/{id}", author.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(author.getFirstName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAuthor() throws Exception {
        Long authorId = 1L;
        given(authorService.findAuthorById(authorId)).willReturn(Optional.empty());
        Author author =
                Author.builder()
                        .id(authorId)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();

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
        Author author =
                Author.builder()
                        .id(authorId)
                        .firstName("First Author")
                        .lastName("lastName")
                        .email("junit1@email.com")
                        .build();
        given(authorService.findAuthorById(authorId)).willReturn(Optional.of(author));
        doNothing().when(authorService).deleteAuthorById(author.getId());

        this.mockMvc
                .perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(author.getFirstName())));
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
