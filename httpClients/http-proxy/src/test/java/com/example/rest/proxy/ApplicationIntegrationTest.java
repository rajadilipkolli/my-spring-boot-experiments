package com.example.rest.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rest.proxy.common.AbstractIntegrationTest;

import org.junit.jupiter.api.Test;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(sqlContainer.isRunning()).isTrue();
    }
}
