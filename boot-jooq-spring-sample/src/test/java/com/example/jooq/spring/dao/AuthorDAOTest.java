package com.example.jooq.spring.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jooq.spring.common.SQLContainerConfig;
import com.example.jooq.spring.db.tables.records.AuthorsRecord;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@JooqTest
@Import({SQLContainerConfig.class, AuthorDAO.class})
@Transactional
class AuthorDAOTest {

    @Autowired
    private AuthorDAO authorDAO;

    @Test
    void testSaveAndFindById() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("George Orwell");
        author.setEmail("george.orwell@example.com");
        author.setCreatedAt(OffsetDateTime.parse("2026-01-15T09:30:00+05:30"));

        int rows = authorDAO.save(author);
        assertThat(rows).isEqualTo(1);
        assertThat(author.getId()).isNotNull();

        Optional<AuthorsRecord> found = authorDAO.findById(author.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("George Orwell");
        assertThat(found.get().getEmail()).isEqualTo("george.orwell@example.com");
        assertThat(found.get().getCreatedAt().toInstant())
                .isEqualTo(author.getCreatedAt().toInstant());
    }

    @Test
    void testFindAll() {
        AuthorsRecord author1 = new AuthorsRecord();
        author1.setName("Author One");
        author1.setEmail("author1@example.com");
        author1.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author1);

        AuthorsRecord author2 = new AuthorsRecord();
        author2.setName("Author Two");
        author2.setEmail("author2@example.com");
        author2.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author2);

        List<AuthorsRecord> authors = authorDAO.findAll();
        assertThat(authors).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testFindByEmail() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("Jane Austen");
        author.setEmail("jane.austen@example.com");
        author.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author);

        Optional<AuthorsRecord> found = authorDAO.findByEmail("jane.austen@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Austen");
    }

    @Test
    void testDelete() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("Temporary Author");
        author.setEmail("temp.author@example.com");
        author.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author);

        Long id = author.getId();
        int deleted = authorDAO.delete(author);
        assertThat(deleted).isEqualTo(1);
        assertThat(authorDAO.findById(id)).isEmpty();
    }

    @Test
    void testDeleteById() {
        AuthorsRecord author = new AuthorsRecord();
        author.setName("Delete By Id Author");
        author.setEmail("deletebyid@example.com");
        author.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author);

        Long id = author.getId();
        int deleted = authorDAO.deleteById(id);
        assertThat(deleted).isEqualTo(1);
        assertThat(authorDAO.findById(id)).isEmpty();
    }

    @Test
    void testCount() {
        int initialCount = authorDAO.count();

        AuthorsRecord author = new AuthorsRecord();
        author.setName("Count Author");
        author.setEmail("count@example.com");
        author.setCreatedAt(OffsetDateTime.now());
        authorDAO.save(author);

        assertThat(authorDAO.count()).isEqualTo(initialCount + 1);
    }
}
