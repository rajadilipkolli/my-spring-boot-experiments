package com.example.multitenancy.config.multitenant;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component("tenantIdentifierResolver")
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    private String currentTenant = "unknown";

    public void setCurrentTenant(@Nullable String tenant) {
        currentTenant = Objects.requireNonNullElse(tenant, "unknown");
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        return currentTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
