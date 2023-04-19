package com.example.multitenancy.config.multitenant;

import com.example.multitenancy.utils.DatabaseType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import javax.sql.DataSource;

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
        targetDataSources.put(DatabaseType.primary.name(), primaryDataSource);
        targetDataSources.put(DatabaseType.dbsystc.name(), primaryDataSource);
        targetDataSources.put(DatabaseType.dbsystp.name(), primaryDataSource);
        targetDataSources.put(DatabaseType.dbsystv.name(), primaryDataSource);
        targetDataSources.put(DatabaseType.test1.name(), secondaryDataSource);
        targetDataSources.put(DatabaseType.test2.name(), secondaryDataSource);
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected String determineCurrentLookupKey() {
        return tenantIdentifierResolver.resolveCurrentTenantIdentifier();
    }
}
