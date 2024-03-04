package com.example.bootbatchjpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bootbatchjpa.common.TestContainersConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@DataJpaTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.test.database.replace=none"
        })
@ImportTestcontainers(TestContainersConfig.class)
class SchemaValidationTest {

    @Autowired private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
    }
}
