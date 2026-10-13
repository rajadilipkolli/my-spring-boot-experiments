package com.example.jooq.spring.dao;

import static com.example.jooq.spring.db.Tables.BOOKS;

import ch.martinelli.oss.jooqspring.JooqDAO;
import com.example.jooq.spring.db.tables.Books;
import com.example.jooq.spring.db.tables.records.BooksRecord;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class BookDAO extends JooqDAO<Books, BooksRecord, Long> {

    public BookDAO(DSLContext dslContext) {
        super(dslContext, BOOKS);
    }

    @Transactional(readOnly = true)
    public List<BooksRecord> findByAuthorId(Long authorId) {
        return dslContext.selectFrom(table).where(BOOKS.AUTHOR_ID.eq(authorId)).fetch();
    }

    @Transactional(readOnly = true)
    public Optional<BooksRecord> findByIsbn(String isbn) {
        return dslContext.selectFrom(table).where(BOOKS.ISBN.eq(isbn)).fetchOptional();
    }

    @Transactional(readOnly = true)
    public List<BooksRecord> findByPublishedYearRange(int startYear, int endYear) {
        return dslContext
                .selectFrom(table)
                .where(BOOKS.PUBLISHED_YEAR.between(startYear, endYear))
                .fetch();
    }

    @Transactional(readOnly = true)
    public int countByAuthorId(Long authorId) {
        return count(BOOKS.AUTHOR_ID.eq(authorId));
    }

    @Transactional(readOnly = true)
    public List<BooksRecord> findAll() {
        return dslContext.selectFrom(table).orderBy(BOOKS.ID.asc()).fetch();
    }
}
