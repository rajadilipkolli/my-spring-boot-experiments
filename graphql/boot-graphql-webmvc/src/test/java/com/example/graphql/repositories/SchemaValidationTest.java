package com.example.graphql.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.graphql.common.TestContainersConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
@ImportTestcontainers(TestContainersConfig.class)
class SchemaValidationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull().isInstanceOf(HikariDataSource.class);
    }
}
