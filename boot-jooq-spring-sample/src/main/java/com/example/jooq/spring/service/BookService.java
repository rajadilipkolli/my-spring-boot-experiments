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

    public BookService(BookDAO bookDAO, AuthorDAO authorDAO) {
        this.bookDAO = bookDAO;
        this.authorDAO = authorDAO;
    }

    public BookResponse createBook(CreateBookRequest request) {
        authorDAO
                .findById(request.authorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + request.authorId()));

        BooksRecord record = new BooksRecord();
        record.setTitle(request.title());
        record.setIsbn(request.isbn());
        record.setAuthorId(request.authorId());
        record.setPublishedYear(request.publishedYear());
        record.setCreatedAt(LocalDateTime.now());
        bookDAO.save(record);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return bookDAO.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookDAO.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findByAuthor(Long authorId) {
        authorDAO
                .findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + authorId));
        return bookDAO.findByAuthorId(authorId).stream().map(this::toResponse).toList();
    }

    public void deleteBook(Long id) {
        bookDAO.findById(id).orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        bookDAO.deleteById(id);
    }

    private BookResponse toResponse(BooksRecord record) {
        return new BookResponse(
                record.getId(),
                record.getTitle(),
                record.getIsbn(),
                record.getAuthorId(),
                record.getPublishedYear(),
                record.getCreatedAt());
    }
}
