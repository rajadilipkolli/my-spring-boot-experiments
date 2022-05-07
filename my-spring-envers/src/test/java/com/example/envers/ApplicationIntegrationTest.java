package com.example.envers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.envers.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(sqlContainer.isRunning()).isTrue();
    }
}
