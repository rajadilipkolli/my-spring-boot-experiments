package com.example.multitenancy.config.multitenant;

import com.example.multitenancy.utils.DatabaseType;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

@Component
@DependsOnDatabaseInitialization
public class TenantRoutingDatasource extends AbstractRoutingDataSource {

    private final TenantIdentifierResolver tenantIdentifierResolver;

    TenantRoutingDatasource(
            TenantIdentifierResolver tenantIdentifierResolver,
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
        // commenting default so that db connection is made as per the required type
        // setDefaultTargetDataSource(primaryDataSource);

        HashMap<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DatabaseType.PRIMARY.getSchemaName(), primaryDataSource);
        targetDataSources.put(DatabaseType.DBSYSTC.getSchemaName(), primaryDataSource);
        targetDataSources.put(DatabaseType.DBSYSTP.getSchemaName(), primaryDataSource);
        targetDataSources.put(DatabaseType.DBSYSTV.getSchemaName(), primaryDataSource);
        targetDataSources.put(DatabaseType.SECONDARY.getSchemaName(), secondaryDataSource);
        targetDataSources.put(DatabaseType.SCHEMA1.getSchemaName(), secondaryDataSource);
        targetDataSources.put(DatabaseType.SCHEMA2.getSchemaName(), secondaryDataSource);
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected String determineCurrentLookupKey() {
        return tenantIdentifierResolver.resolveCurrentTenantIdentifier();
    }
}
