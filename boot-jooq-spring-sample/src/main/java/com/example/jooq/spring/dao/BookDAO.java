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

    /** Creates a book DAO using the supplied database context. */
    public BookDAO(DSLContext dslContext) {
        super(dslContext, BOOKS);
    }

    /**
     * Retrieves books for an author without checking whether the author exists.
     *
     * @return matching books, or an empty list if none exist; order is unspecified
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public List<BooksRecord> findByAuthorId(Long authorId) {
        return dslContext.selectFrom(table).where(BOOKS.AUTHOR_ID.eq(authorId)).fetch();
    }

    /**
     * Finds a book by ISBN.
     *
     * @return the matching book, or empty if no ISBN matches
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public Optional<BooksRecord> findByIsbn(String isbn) {
        return dslContext.selectFrom(table).where(BOOKS.ISBN.eq(isbn)).fetchOptional();
    }

    /**
     * Retrieves books whose publication year falls within the inclusive range.
     *
     * @param startYear inclusive lower year bound
     * @param endYear inclusive upper year bound
     * @return matching books in unspecified order, excluding unknown publication years;
     *     empty if no years match or the bounds are reversed
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public List<BooksRecord> findByPublishedYearRange(int startYear, int endYear) {
        return dslContext
                .selectFrom(table)
                .where(BOOKS.PUBLISHED_YEAR.between(startYear, endYear))
                .fetch();
    }

    /**
     * Counts an author's books, returning zero if none match or the author does not exist.
     *
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public int countByAuthorId(Long authorId) {
        return count(BOOKS.AUTHOR_ID.eq(authorId));
    }

    /**
     * Retrieves all books in ascending ID order, or an empty list if none exist.
     *
     * @throws org.springframework.dao.DataAccessException if the database query fails
     */
    @Transactional(readOnly = true)
    public List<BooksRecord> findAll() {
        return dslContext.selectFrom(table).orderBy(BOOKS.ID.asc()).fetch();
    }
}
