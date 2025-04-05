package com.example.demo.readreplica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReadReplicaApplicationTests {

    @Autowired private DataSource dataSource;

    private JdbcTemplate primaryJdbcTemplate;

    private JdbcTemplate replicaJdbcTemplate;

    private final String insertSQL =
            "INSERT INTO articles (id, title, authored, published) VALUES (?, ?, ?, ?)";

    private final String countSQL = "SELECT COUNT(1) FROM articles";

    @BeforeAll
    void setUp() {
        setupJdbcTemplates(dataSource);
    }

    private void setupJdbcTemplates(DataSource dataSource) {
        assertThat(dataSource)
                .isNotNull()
                .withFailMessage("DataSource must not be null")
                .isInstanceOf(LazyConnectionDataSourceProxy.class);

        LazyConnectionDataSourceProxy lazyProxy = (LazyConnectionDataSourceProxy) dataSource;

        // Setup primary template
        DataSource targetDataSource = lazyProxy.getTargetDataSource();
        assertThat(targetDataSource)
                .isNotNull()
                .withFailMessage("Target DataSource must not be null")
                .isInstanceOf(DataSource.class);

        primaryJdbcTemplate = new JdbcTemplate(targetDataSource);

        // Setup replica template
        Object readOnlyDataSource = ReflectionTestUtils.getField(lazyProxy, "readOnlyDataSource");
        assertThat(readOnlyDataSource)
                .isNotNull()
                .withFailMessage("Read-only DataSource must not be null")
                .isInstanceOf(DataSource.class);

        replicaJdbcTemplate = new JdbcTemplate((DataSource) readOnlyDataSource);
    }

    @Test
    @Order(1)
    void contextLoads() {
        Integer noOfRows = primaryJdbcTemplate.queryForObject(countSQL, Integer.class);
        // loaded 3 from liquibase
        assertThat(noOfRows).isEqualTo(3);
        noOfRows = replicaJdbcTemplate.queryForObject(countSQL, Integer.class);
        assertThat(noOfRows).isEqualTo(3);
    }

    @Order(2)
    @Test
    void shouldNotInsertViaReadReplica() {
        Object[] params = {4, "Junit", LocalDateTime.now(), null};
        assertThatThrownBy(() -> replicaJdbcTemplate.update(insertSQL, params))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("cannot execute INSERT in a read-only transaction");
    }

    @Order(3)
    @Test
    void shouldInsertViaPrimary() {
        Object[] params = {4, "Junit", LocalDateTime.now(), null};
        int rowsAffected = primaryJdbcTemplate.update(insertSQL, params);
        assertThat(rowsAffected).isEqualTo(1);
        Integer noOfRows = primaryJdbcTemplate.queryForObject(countSQL, Integer.class);
        assertThat(noOfRows).isEqualTo(4);
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> {
                            Integer totalCount =
                                    replicaJdbcTemplate.queryForObject(countSQL, Integer.class);
                            assertThat(totalCount).isEqualTo(4);
                        });
    }

    @Order(4)
    @Test
    void shouldNotDeleteViaReplica() {
        String deleteSQL = "DELETE FROM articles WHERE id = ?";
        Object[] params = {4};
        assertThatThrownBy(() -> replicaJdbcTemplate.update(deleteSQL, params))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("cannot execute DELETE in a read-only transaction");
    }

    @Order(5)
    @Test
    void shouldDeleteViaPrimary() {
        String deleteSQL = "DELETE FROM articles WHERE id = ?";
        Object[] params = {4};
        int rowsAffected = primaryJdbcTemplate.update(deleteSQL, params);
        assertThat(rowsAffected).isEqualTo(1);
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(
                        () -> {
                            Integer noOfRows =
                                    replicaJdbcTemplate.queryForObject(countSQL, Integer.class);
                            assertThat(noOfRows).isEqualTo(3);
                        });
    }
}
