package com.example.locks;

import com.example.locks.common.ContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.test.database.replace=none"})
@ImportTestcontainers(ContainersConfig.class)
class SchemaValidationTest {

    @Test
    void validateJpaMappingsWithDbSchema() {}
}
