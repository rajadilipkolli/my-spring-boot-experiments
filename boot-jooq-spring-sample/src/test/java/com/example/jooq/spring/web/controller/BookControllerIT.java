package com.example.jooq.spring.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.spring.common.AbstractIntegrationTest;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import com.example.jooq.spring.db.tables.records.BooksRecord;
import com.example.jooq.spring.model.request.CreateBookRequest;
import com.example.jooq.spring.model.response.BookResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class BookControllerIT extends AbstractIntegrationTest {

    private Long authorId;

    @BeforeEach
    void setUp() {
        authorDAO.findAll().forEach(author -> {
            bookDAO.findByAuthorId(author.getId()).forEach(bookDAO::delete);
            authorDAO.delete(author);
        });

        AuthorsRecord author = new AuthorsRecord();
        author.setName("Brandon Sanderson");
        author.setEmail("sanderson@example.com");
        author.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author);
        this.authorId = author.getId();
    }

    @Test
    void shouldCreateBookSuccessfully() {
        CreateBookRequest request =
                new CreateBookRequest("Mistborn: The Final Empire", "978-0765311788", authorId, 2006);

        var result = mockMvcTester
                .post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request))
                .exchange();

        assertThat(result)
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(BookResponse.class)
                .satisfies(bookResponse -> {
                    assertThat(bookResponse.id()).isNotNull();
                    assertThat(bookResponse.title()).isEqualTo("Mistborn: The Final Empire");
                    assertThat(bookResponse.authorId()).isEqualTo(authorId);
                    assertThat(result)
                            .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/books/" + bookResponse.id());
                });
    }

    @Test
    void shouldReturnNotFoundWhenCreatingBookForNonExistentAuthor() {
        CreateBookRequest request = new CreateBookRequest("Ghost Book", "ISBN-GHOST", 999999L, 2020);

        mockMvcTester
                .post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetAllBooks() {
        BooksRecord book = new BooksRecord();
        book.setTitle("The Way of Kings");
        book.setIsbn("978-0765326355");
        book.setAuthorId(authorId);
        book.setPublishedYear(2010);
        book.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book);

        mockMvcTester
                .get()
                .uri("/api/books")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asArray()
                .hasSize(1);
    }

    @Test
    void shouldGetBookById() {
        BooksRecord book = new BooksRecord();
        book.setTitle("Words of Radiance");
        book.setIsbn("978-0765326362");
        book.setAuthorId(authorId);
        book.setPublishedYear(2014);
        book.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book);

        mockMvcTester
                .get()
                .uri("/api/books/{id}", book.getId())
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(BookResponse.class)
                .satisfies(bookResponse -> {
                    assertThat(bookResponse.title()).isEqualTo("Words of Radiance");
                });
    }

    @Test
    void shouldGetBooksByAuthor() {
        BooksRecord book1 = new BooksRecord();
        book1.setTitle("Oathbringer");
        book1.setIsbn("978-0765326379");
        book1.setAuthorId(authorId);
        book1.setPublishedYear(2017);
        book1.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book1);

        mockMvcTester
                .get()
                .uri("/api/books/by-author/{authorId}", authorId)
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asArray()
                .hasSize(1);
    }

    @Test
    void shouldDeleteBookSuccessfully() {
        BooksRecord book = new BooksRecord();
        book.setTitle("Rhythm of War");
        book.setIsbn("978-0765326386");
        book.setAuthorId(authorId);
        book.setPublishedYear(2020);
        book.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book);

        mockMvcTester.delete().uri("/api/books/{id}", book.getId()).assertThat().hasStatus(HttpStatus.NO_CONTENT);

        assertThat(bookDAO.findById(book.getId())).isEmpty();
    }
}
