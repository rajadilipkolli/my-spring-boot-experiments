package com.example.jooq.spring.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.spring.common.AbstractIntegrationTest;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import com.example.jooq.spring.db.tables.records.BooksRecord;
import com.example.jooq.spring.model.request.CreateAuthorRequest;
import com.example.jooq.spring.model.response.AuthorResponse;
import com.example.jooq.spring.model.response.AuthorWithBooksResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AuthorControllerIT extends AbstractIntegrationTest {

    @BeforeEach
    void setUp() {
        authorDAO.findAll().forEach(author -> {
            bookDAO.findByAuthorId(author.getId()).forEach(bookDAO::delete);
            authorDAO.delete(author);
        });
    }

    @Test
    void shouldCreateAuthorSuccessfully() {
        CreateAuthorRequest request = new CreateAuthorRequest("Isaac Asimov", "asimov@example.com");

        var result = mockMvcTester
                .post()
                .uri("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request));

        result.assertThat()
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(AuthorResponse.class)
                .satisfies(authorResponse -> {
                    assertThat(authorResponse.id()).isNotNull();
                    assertThat(authorResponse.name()).isEqualTo("Isaac Asimov");
                    assertThat(authorResponse.email()).isEqualTo("asimov@example.com");
                    result.assertThat()
                            .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/authors/" + authorResponse.id());
                });
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAuthorWithInvalidData() {
        CreateAuthorRequest request = new CreateAuthorRequest("", "not-an-email");

        mockMvcTester
                .post()
                .uri("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetAuthorById() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("Arthur C. Clarke");
        author.setEmail("clarke@example.com");
        author.setCreatedAt(LocalDateTime.now());
        authorDAO.save(author);

        mockMvcTester
                .get()
                .uri("/api/authors/{id}", author.getId())
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(AuthorResponse.class)
                .satisfies(authorResponse -> {
                    assertThat(authorResponse.name()).isEqualTo("Arthur C. Clarke");
                    assertThat(authorResponse.email()).isEqualTo("clarke@example.com");
                });
    }

    @Test
    void shouldReturnNotFoundForNonExistentAuthor() {
        mockMvcTester.get().uri("/api/authors/{id}", 999999L).assertThat().hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldListAuthorsWithPagination() {
        AuthorsRecord author1 = new AuthorsRecord();
        author1.setName("Author 1");
        author1.setEmail("author1@test.com");
        author1.setCreatedAt(LocalDateTime.now());
        authorDAO.save(author1);

        AuthorsRecord author2 = new AuthorsRecord();
        author2.setName("Author 2");
        author2.setEmail("author2@test.com");
        author2.setCreatedAt(LocalDateTime.now());
        authorDAO.save(author2);

        mockMvcTester
                .get()
                .uri("/api/authors?offset={offset}&limit={limit}", 0, 10)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asArray()
                .hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateAuthorSuccessfully() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("Original Name");
        author.setEmail("original@example.com");
        author.setCreatedAt(LocalDateTime.now());
        authorDAO.save(author);

        CreateAuthorRequest updateRequest = new CreateAuthorRequest("Updated Name", "updated@example.com");

        mockMvcTester
                .put()
                .uri("/api/authors/{id}", author.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateRequest))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(AuthorResponse.class)
                .satisfies(authorResponse -> {
                    assertThat(authorResponse.name()).isEqualTo("Updated Name");
                    assertThat(authorResponse.email()).isEqualTo("updated@example.com");
                });
    }

    @Test
    void shouldGetAuthorWithBooks() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("Frank Herbert");
        author.setEmail("herbert@example.com");
        author.setCreatedAt(LocalDateTime.now());
        authorDAO.save(author);

        BooksRecord book = new BooksRecord();
        book.setTitle("Dune");
        book.setIsbn("978-0441172719");
        book.setAuthorId(author.getId());
        book.setPublishedYear(1965);
        book.setCreatedAt(LocalDateTime.now());
        bookDAO.save(book);

        mockMvcTester
                .get()
                .uri("/api/authors/{id}/books", author.getId())
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(AuthorWithBooksResponse.class)
                .satisfies(res -> {
                    assertThat(res.name()).isEqualTo("Frank Herbert");
                    assertThat(res.books()).hasSize(1);
                    assertThat(res.books().getFirst().title()).isEqualTo("Dune");
                });
    }

    @Test
    void shouldDeleteAuthorSuccessfully() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("To Delete");
        author.setEmail("delete@example.com");
        author.setCreatedAt(LocalDateTime.now());
        authorDAO.save(author);

        mockMvcTester
                .delete()
                .uri("/api/authors/{id}", author.getId())
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        assertThat(authorDAO.findById(author.getId())).isEmpty();
    }
}
