package com.example.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.config.multitenant.TenantRoutingDatasource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("tenantRoutingDatasource") private DataSource tenantRoutingDatasource;

    @Test
    void contextLoads() {
        assertThat(tenantRoutingDatasource).isNotNull();
        assertThat(((TenantRoutingDatasource) tenantRoutingDatasource).getResolvedDataSources())
                .isNotEmpty()
                .hasSize(7);
    }
}
