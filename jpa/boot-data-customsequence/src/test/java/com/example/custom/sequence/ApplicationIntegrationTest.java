package com.example.custom.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.jupiter.api.Test;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(dataSource).isInstanceOf(ProxyDataSource.class);
    }
}
