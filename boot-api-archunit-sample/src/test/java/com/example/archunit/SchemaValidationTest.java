package com.example.archunit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.archunit.common.ContainersConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(showSql = false, properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import(ContainersConfig.class)
class SchemaValidationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void validateJpaMappingsWithDbSchema() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
    }
}
