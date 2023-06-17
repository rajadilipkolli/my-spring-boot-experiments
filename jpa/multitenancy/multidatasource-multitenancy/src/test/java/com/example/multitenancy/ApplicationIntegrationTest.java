package com.example.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.multitenancy.common.AbstractIntegrationTest;
import com.example.multitenancy.config.multitenant.TenantRoutingDatasource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TenantRoutingDatasource tenantRoutingDatasource;

    @Test
    void contextLoads() {
        assertThat(tenantRoutingDatasource).isNotNull();
        assertThat(tenantRoutingDatasource.getResolvedDataSources()).isNotEmpty().hasSize(6);
    }
}
