package com.example.jndi;

import com.example.jndi.common.ContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.test.database.replace=none"})
@Import(ContainersConfig.class)
class SchemaValidationTest {

    @Test
    void validateJpaMappingsWithDbSchema() {}
}
