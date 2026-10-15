package com.example.jooq.spring.service;

import com.example.jooq.spring.dao.AuthorDAO;
import com.example.jooq.spring.dao.BookDAO;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import com.example.jooq.spring.model.request.CreateAuthorRequest;
import com.example.jooq.spring.model.response.AuthorResponse;
import com.example.jooq.spring.model.response.AuthorWithBooksResponse;
import com.example.jooq.spring.model.response.BookResponse;
import com.example.jooq.spring.web.exception.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthorService {

    private final AuthorDAO authorDAO;
    private final BookDAO bookDAO;

    /** Creates the service with access to authors and their books. */
    public AuthorService(AuthorDAO authorDAO, BookDAO bookDAO) {
        this.authorDAO = authorDAO;
        this.bookDAO = bookDAO;
    }

    /**
     * Persists a new author with the current creation time including its UTC offset.
     *
     * @return the saved author's values, including its generated ID
     * @throws org.springframework.dao.DataIntegrityViolationException if the author violates database
     *     constraints, such as email uniqueness
     * @throws org.springframework.dao.DataAccessException if another database failure occurs
     */
    public AuthorResponse createAuthor(CreateAuthorRequest request) {
        AuthorsRecord record = new AuthorsRecord();
        record.setName(request.name());
        record.setEmail(request.email());
        record.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(record);
        return AuthorResponse.toResponse(record);
    }

    /**
     * Retrieves an author by ID.
     *
     * @throws EntityNotFoundException if the author does not exist
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return authorDAO
                .findById(id)
                .map(AuthorResponse::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
    }

    /**
     * Retrieves a page of authors in ascending ID order.
     *
     * @param offset number of rows to skip, starting from zero
     * @param limit maximum number of rows to return
     * @return the selected authors, or an empty list if the page has no rows
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll(int offset, int limit) {
        return authorDAO.findAll(offset, limit).stream()
                .map(AuthorResponse::toResponse)
                .toList();
    }

    /**
     * Retrieves all authors in ascending ID order, or an empty list if none exist.
     *
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorDAO.findAll().stream().map(AuthorResponse::toResponse).toList();
    }

    /**
     * Replaces an existing author's name and email, preserving its creation time.
     *
     * @return the saved author's values
     * @throws EntityNotFoundException if the author does not exist
     * @throws org.springframework.dao.DataIntegrityViolationException if the update violates database
     *     constraints, such as email uniqueness
     * @throws org.springframework.dao.DataAccessException if another database failure occurs
     */
    public AuthorResponse updateAuthor(Long id, CreateAuthorRequest request) {
        AuthorsRecord record = authorDAO
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
        record.setName(request.name());
        record.setEmail(request.email());
        authorDAO.save(record);
        return AuthorResponse.toResponse(record);
    }

    /**
     * Deletes an existing author and its books through the database's cascading foreign key.
     *
     * @throws EntityNotFoundException if the author does not exist
     * @throws org.springframework.dao.DataAccessException if a database operation fails
     */
    public void deleteAuthor(Long id) {
        boolean existsById = authorDAO.existsById(id);
        if (!existsById) {
            throw new EntityNotFoundException("Author not found with id: " + id);
        }
        authorDAO.deleteById(id);
    }

    /**
     * Retrieves an author with its books in unspecified book order.
     *
     * @return the author and its books, with an empty book list if none exist
     * @throws EntityNotFoundException if the author does not exist
     * @throws org.springframework.dao.DataAccessException if a database query fails
     */
    @Transactional(readOnly = true)
    public AuthorWithBooksResponse findAuthorWithBooks(Long id) {
        AuthorsRecord author = authorDAO
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
        List<BookResponse> books = bookDAO.findByAuthorId(id).stream()
                .map(BookResponse::toBookResponse)
                .toList();
        return AuthorWithBooksResponse.from(author, books);
    }
}
