package com.example.locks;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.test.database.replace=none",
            "spring.datasource.url=jdbc:tc:postgresql:16.0-alpine:///db"
        })
class SchemaValidationTest {

    @Test
    void validateJpaMappingsWithDbSchema() {}
}
