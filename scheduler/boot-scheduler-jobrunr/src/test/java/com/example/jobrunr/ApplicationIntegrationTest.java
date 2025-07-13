package com.example.jobrunr;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jobrunr.common.AbstractIntegrationTest;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull();
    }
}
