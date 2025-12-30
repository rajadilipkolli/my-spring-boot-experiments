package com.example.custom.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(dataSource).isInstanceOf(LazyConnectionDataSourceProxy.class);
    }
}
