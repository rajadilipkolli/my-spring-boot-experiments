package com.example.jooq.spring.web.controller;

import com.example.jooq.spring.model.request.CreateBookRequest;
import com.example.jooq.spring.model.response.BookResponse;
import com.example.jooq.spring.service.BookService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    /** Creates the book endpoints using the supplied service. */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Lists all books in ascending ID order, or an empty list if none exist.
     *
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.findAll();
    }

    /**
     * Returns HTTP 200 with the requested book.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the book does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    /**
     * Lists books for an existing author in unspecified order, or an empty list if it has none.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the author does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataAccessException if a database query fails
     */
    @GetMapping("/by-author/{authorId}")
    public List<BookResponse> getBooksByAuthor(@PathVariable Long authorId) {
        return bookService.findByAuthor(authorId);
    }

    /**
     * Creates a book and returns HTTP 201 with its values and a Location header for its URL.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the requested author
     *     does not exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are
     *     violated; the exception handler converts this to HTTP 409
     * @throws org.springframework.dao.DataAccessException if another database failure occurs
     */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookResponse created = bookService.createBook(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Deletes the book and returns HTTP 204 with no body.
     *
     * @throws com.example.jooq.spring.web.exception.EntityNotFoundException if the book does not
     *     exist; the exception handler converts this to HTTP 404
     * @throws org.springframework.dao.DataAccessException if a database operation fails
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
