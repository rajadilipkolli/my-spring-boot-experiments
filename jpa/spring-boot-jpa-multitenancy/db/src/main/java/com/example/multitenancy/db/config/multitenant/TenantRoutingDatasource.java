package com.example.multitenancy.db.config.multitenant;

import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

@Component
public class TenantRoutingDatasource extends AbstractRoutingDataSource {

    private final TenantIdentifierResolver tenantIdentifierResolver;

    TenantRoutingDatasource(
            TenantIdentifierResolver tenantIdentifierResolver,
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;

        setDefaultTargetDataSource(primaryDataSource);

        HashMap<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("postgres", primaryDataSource);
        targetDataSources.put("mariadb", secondaryDataSource);
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected String determineCurrentLookupKey() {
        return tenantIdentifierResolver.resolveCurrentTenantIdentifier();
    }
}
