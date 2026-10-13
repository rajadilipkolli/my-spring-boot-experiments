package com.example.jooq.spring.service;

import com.example.jooq.spring.dao.AuthorDAO;
import com.example.jooq.spring.dao.BookDAO;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import com.example.jooq.spring.model.request.CreateAuthorRequest;
import com.example.jooq.spring.model.response.AuthorResponse;
import com.example.jooq.spring.model.response.AuthorWithBooksResponse;
import com.example.jooq.spring.model.response.BookResponse;
import com.example.jooq.spring.web.exception.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthorService {

    private final AuthorDAO authorDAO;
    private final BookDAO bookDAO;

    public AuthorService(AuthorDAO authorDAO, BookDAO bookDAO) {
        this.authorDAO = authorDAO;
        this.bookDAO = bookDAO;
    }

    public AuthorResponse createAuthor(CreateAuthorRequest request) {
        AuthorsRecord record = new AuthorsRecord();
        record.setName(request.name());
        record.setEmail(request.email());
        record.setCreatedAt(LocalDateTime.now());
        authorDAO.save(record);
        return AuthorResponse.toResponse(record);
    }

    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return authorDAO
                .findById(id)
                .map(AuthorResponse::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll(int offset, int limit) {
        return authorDAO.findAll(offset, limit).stream()
                .map(AuthorResponse::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorDAO.findAll().stream().map(AuthorResponse::toResponse).toList();
    }

    public AuthorResponse updateAuthor(Long id, CreateAuthorRequest request) {
        AuthorsRecord record = authorDAO
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
        record.setName(request.name());
        record.setEmail(request.email());
        authorDAO.save(record);
        return AuthorResponse.toResponse(record);
    }

    public void deleteAuthor(Long id) {
        authorDAO.findById(id).orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
        authorDAO.deleteById(id);
    }

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
