package com.example.multitenancy.schema.config.multitenancy;

import static com.example.multitenancy.schema.config.multitenancy.TenantFilter.CURRENT_TENANT;

import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private static final String UNKNOWN = "unknown";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return CURRENT_TENANT.isBound() ? CURRENT_TENANT.get() : UNKNOWN;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
