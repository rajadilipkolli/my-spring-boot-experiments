package com.example.jooq.spring.dao;

import static com.example.jooq.spring.db.Tables.AUTHORS;

import ch.martinelli.oss.jooqspring.JooqDAO;
import com.example.jooq.spring.db.tables.Authors;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class AuthorDAO extends JooqDAO<Authors, AuthorsRecord, Long> {

    public AuthorDAO(DSLContext dslContext) {
        super(dslContext, AUTHORS);
    }

    @Transactional(readOnly = true)
    public Optional<AuthorsRecord> findByEmail(String email) {
        return dslContext.selectFrom(table).where(AUTHORS.EMAIL.eq(email)).fetchOptional();
    }

    @Transactional(readOnly = true)
    public List<AuthorsRecord> findAll(int offset, int limit) {
        return findAll(offset, limit, List.of(AUTHORS.ID.asc()));
    }

    @Transactional(readOnly = true)
    public List<AuthorsRecord> findAll() {
        return dslContext.selectFrom(table).orderBy(AUTHORS.ID.asc()).fetch();
    }
}
