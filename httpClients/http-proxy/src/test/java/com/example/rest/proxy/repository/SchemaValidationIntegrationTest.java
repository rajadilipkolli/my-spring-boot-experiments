package com.example.rest.proxy.repository;

import com.example.rest.proxy.config.DBTestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(DBTestContainersConfiguration.class)
public class SchemaValidationIntegrationTest {

    @Test
    public void testSchemaValidity() {}
}
