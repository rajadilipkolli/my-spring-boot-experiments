package com.example.custom.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.ContainersConfig;
import com.github.gavlyukovskiy.boot.jdbc.decorator.DecoratedDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.test.database.replace=none"
        })
@Import(ContainersConfig.class)
class SchemaValidationTest {

    @Autowired private DataSource dataSource;

    @Test
    void validateJpaMappingsWithDbSchema() {
        assertThat(dataSource).isInstanceOf(DecoratedDataSource.class);
    }
}
