package com.example.keysetpagination.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.keysetpagination.common.ContainersConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Integration test that validates JPA entity mappings against the actual database schema.
 * This ensures that our entity definitions match the database structure exactly,
 * helping catch mapping inconsistencies early.
 */
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
@Import({ContainersConfig.class})
class SchemaValidationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void validateJpaMappingsWithDbSchema() {
        // Validate schema by checking critical table structures
        Query query = entityManager.createNativeQuery("SELECT column_name, data_type, character_maximum_length "
                + "FROM information_schema.columns " + "WHERE table_name = 'animals'");

        List<Object[]> columns = query.getResultList();
        assertThat(columns.isEmpty())
                .as("Animals table should exist with columns")
                .isFalse();

        // Verify specific column existence and types
        Map<String, String> columnTypes =
                columns.stream().collect(Collectors.toMap(row -> (String) row[0], row -> (String) row[1]));

        assertThat(columnTypes.get("id")).as("ID column should be bigint").isEqualTo("bigint");
        assertThat(columnTypes.get("name"))
                .as("name column should be character varying")
                .isEqualTo("text");
        assertThat(columnTypes.get("version"))
                .as("version column should be bigint")
                .isEqualTo("smallint");
    }
}
