package com.example.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.graphql.common.AbstractIntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull().isInstanceOf(HikariDataSource.class);
    }
}
