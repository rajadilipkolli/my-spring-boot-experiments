package com.example.custom.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.custom.sequence.common.AbstractIntegrationTest;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(dataSource).isInstanceOf(ProxyDataSource.class);
        DataSource unwrapped = ((ProxyDataSource) dataSource).getDataSource();
        assertThat(unwrapped).isInstanceOf(LazyConnectionDataSourceProxy.class);
    }
}
