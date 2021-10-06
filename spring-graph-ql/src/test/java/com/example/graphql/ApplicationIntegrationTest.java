package com.example.graphql;

import com.example.graphql.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }
}
