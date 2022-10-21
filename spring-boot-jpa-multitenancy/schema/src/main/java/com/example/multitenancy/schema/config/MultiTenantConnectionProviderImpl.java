package com.example.multitenancy.schema.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultiTenantConnectionProviderImpl
        implements MultiTenantConnectionProvider, HibernatePropertiesCustomizer {

    private final DataSource dataSource;
    private final TenantIdentifierResolver tenantIdentifierResolver;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return getConnection("PUBLIC");
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String schema) throws SQLException {
        final Connection connection = dataSource.getConnection();
        // Tenant Id is not getting cleared hence setting the value based on tenantId of Request
        if ("PUBLIC".equals(schema)) {
            connection.setSchema(schema);
        } else {
            connection.setSchema(tenantIdentifierResolver.resolveCurrentTenantIdentifier());
        }
        return connection;
    }

    @Override
    public void releaseConnection(String s, Connection connection) throws SQLException {
        connection.setSchema("PUBLIC");
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        throw new UnsupportedOperationException("Can't unwrap this.");
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}
