package com.example.choasmonkey;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.choasmonkey.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(sqlContainer.isRunning()).isTrue();
    }
}
