package com.example.restdocs;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.restdocs.common.AbstractIntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
    }
}
