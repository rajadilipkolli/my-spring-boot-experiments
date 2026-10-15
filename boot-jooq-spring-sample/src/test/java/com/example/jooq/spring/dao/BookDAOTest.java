package com.example.jooq.spring.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.spring.common.SQLContainerConfig;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import com.example.jooq.spring.db.tables.records.BooksRecord;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@JooqTest
@Import({SQLContainerConfig.class, AuthorDAO.class, BookDAO.class})
@Transactional
class BookDAOTest {

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private AuthorDAO authorDAO;

    private Long authorId;

    @BeforeEach
    void setUp() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("J.K. Rowling");
        author.setEmail("jk.rowling@example.com");
        author.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author);
        this.authorId = author.getId();
    }

    @Test
    void testSaveAndFindByAuthorId() {
        BooksRecord book = new BooksRecord();
        book.setTitle("Harry Potter and the Philosopher's Stone");
        book.setIsbn("978-0747532699");
        book.setAuthorId(authorId);
        book.setPublishedYear(1997);
        book.setCreatedAt(OffsetDateTime.parse("2026-01-15T09:30:00-04:00"));
        bookDAO.save(book);

        List<BooksRecord> books = bookDAO.findByAuthorId(authorId);
        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");
        assertThat(books.getFirst().getIsbn()).isEqualTo("978-0747532699");
        assertThat(books.getFirst().getCreatedAt()).isEqualTo(book.getCreatedAt());
    }

    @Test
    void testFindByIsbn() {
        BooksRecord book = new BooksRecord();
        book.setTitle("Harry Potter and the Chamber of Secrets");
        book.setIsbn("978-0747538493");
        book.setAuthorId(authorId);
        book.setPublishedYear(1998);
        book.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book);

        Optional<BooksRecord> found = bookDAO.findByIsbn("978-0747538493");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Harry Potter and the Chamber of Secrets");
        assertThat(found.get().getPublishedYear()).isEqualTo(1998);
    }

    @Test
    void testCountByAuthorId() {
        assertThat(bookDAO.countByAuthorId(authorId)).isZero();

        BooksRecord book1 = new BooksRecord();
        book1.setTitle("Book One");
        book1.setIsbn("ISBN-001");
        book1.setAuthorId(authorId);
        book1.setPublishedYear(2001);
        book1.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book1);

        BooksRecord book2 = new BooksRecord();
        book2.setTitle("Book Two");
        book2.setIsbn("ISBN-002");
        book2.setAuthorId(authorId);
        book2.setPublishedYear(2002);
        book2.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book2);

        assertThat(bookDAO.countByAuthorId(authorId)).isEqualTo(2);
    }

    @Test
    void testFindByPublishedYearRange() {
        BooksRecord book1 = new BooksRecord();
        book1.setTitle("Old Book");
        book1.setIsbn("ISBN-OLD");
        book1.setAuthorId(authorId);
        book1.setPublishedYear(1990);
        book1.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book1);

        BooksRecord book2 = new BooksRecord();
        book2.setTitle("Mid Book");
        book2.setIsbn("ISBN-MID");
        book2.setAuthorId(authorId);
        book2.setPublishedYear(2000);
        book2.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book2);

        BooksRecord book3 = new BooksRecord();
        book3.setTitle("New Book");
        book3.setIsbn("ISBN-NEW");
        book3.setAuthorId(authorId);
        book3.setPublishedYear(2020);
        book3.setCreatedAt(OffsetDateTime.now());
        bookDAO.save(book3);

        List<BooksRecord> books = bookDAO.findByPublishedYearRange(1995, 2005);
        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle()).isEqualTo("Mid Book");
    }
}
