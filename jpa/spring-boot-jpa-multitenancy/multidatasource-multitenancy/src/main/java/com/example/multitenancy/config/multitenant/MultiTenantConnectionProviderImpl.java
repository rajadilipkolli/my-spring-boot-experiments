package com.example.multitenancy.config.multitenant;

import com.example.multitenancy.utils.DatabaseType;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

@Component("multiTenantConnectionProviderImpl")
public class MultiTenantConnectionProviderImpl
        implements MultiTenantConnectionProvider, HibernatePropertiesCustomizer {

    private final DataSource tenantRoutingDatasource;

    public MultiTenantConnectionProviderImpl(
            @Qualifier("tenantRoutingDatasource") DataSource tenantRoutingDatasource) {
        this.tenantRoutingDatasource = tenantRoutingDatasource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return tenantRoutingDatasource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        var connection = tenantRoutingDatasource.getConnection();
        if (DatabaseType.test1.name().equals(tenantIdentifier)
                || DatabaseType.test2.name().equals(tenantIdentifier)) {
            connection.setSchema(tenantIdentifier);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection)
            throws SQLException {
        if (DatabaseType.test1.name().equals(tenantIdentifier)
                || DatabaseType.test2.name().equals(tenantIdentifier)) {
            connection.setSchema("public");
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Can't unwrap this.");
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}
