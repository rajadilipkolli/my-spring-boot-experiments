package com.example.jooq.spring.service;

import com.example.jooq.spring.dao.AuthorDAO;
import com.example.jooq.spring.dao.BookDAO;
import com.example.jooq.spring.db.tables.records.BooksRecord;
import com.example.jooq.spring.model.request.CreateBookRequest;
import com.example.jooq.spring.model.response.BookResponse;
import com.example.jooq.spring.web.exception.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

    private final BookDAO bookDAO;
    private final AuthorDAO authorDAO;

    /** Creates the service with access to books and their authors. */
    public BookService(BookDAO bookDAO, AuthorDAO authorDAO) {
        this.bookDAO = bookDAO;
        this.authorDAO = authorDAO;
    }

    /**
     * Persists a book for an existing author with the current local creation time.
     *
     * @param request book details; a null publication year represents an unknown year
     * @return the saved book's values, including its generated ID
     * @throws EntityNotFoundException if the requested author does not exist
     * @throws org.springframework.dao.DataIntegrityViolationException if the book violates database
     *     constraints, such as ISBN uniqueness
     * @throws org.springframework.dao.DataAccessException if another database failure occurs
     */
    public BookResponse createBook(CreateBookRequest request) {
        boolean existsById = authorDAO.existsById(request.authorId());
        if (!existsById) {
            throw new EntityNotFoundException("Author not found with id: " + request.authorId());
        }

        BooksRecord record = new BooksRecord();
        record.setTitle(request.title());
        record.setIsbn(request.isbn());
        record.setAuthorId(request.authorId());
        record.setPublishedYear(request.publishedYear());
        record.setCreatedAt(LocalDateTime.now());
        bookDAO.save(record);
        return BookResponse.toBookResponse(record);
    }

    /**
     * Retrieves a book by ID.
     *
     * @throws EntityNotFoundException if the book does not exist
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return bookDAO.findById(id)
                .map(BookResponse::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    /**
     * Retrieves all books in ascending ID order, or an empty list if none exist.
     *
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookDAO.findAll().stream().map(BookResponse::toBookResponse).toList();
    }

    /**
     * Retrieves books for an existing author in unspecified order.
     *
     * @return the author's books, or an empty list if the author has none
     * @throws EntityNotFoundException if the author does not exist
     * @throws org.springframework.dao.DataAccessException if a database query fails
     */
    @Transactional(readOnly = true)
    public List<BookResponse> findByAuthor(Long authorId) {
        boolean existsById = authorDAO.existsById(authorId);
        if (!existsById) {
            throw new EntityNotFoundException("Author not found with id: " + authorId);
        }
        return bookDAO.findByAuthorId(authorId).stream()
                .map(BookResponse::toBookResponse)
                .toList();
    }

    /**
     * Deletes an existing book.
     *
     * @throws EntityNotFoundException if the book does not exist
     * @throws org.springframework.dao.DataAccessException if a database operation fails
     */
    public void deleteBook(Long id) {
        boolean existsById = bookDAO.existsById(id);
        if (!existsById) {
            throw new EntityNotFoundException("Book not found with id: " + id);
        }
        bookDAO.deleteById(id);
    }
}
