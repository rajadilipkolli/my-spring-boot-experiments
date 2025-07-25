package com.example.keysetpagination;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.keysetpagination.common.ContainersConfig;
import com.example.keysetpagination.config.SpringBlazePersistenceConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
@Import({SpringBlazePersistenceConfiguration.class, ContainersConfig.class})
class SchemaValidationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void validateJpaMappingsWithDbSchema() {
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
    }
}
