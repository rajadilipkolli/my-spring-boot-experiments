package com.example.multitenancy.db.config.multitenant;

import java.util.Map;
import java.util.Objects;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    @Override
    public String resolveCurrentTenantIdentifier() {
        return currentTenant.get();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

    public void setCurrentTenant(@Nullable String tenant) {
        currentTenant.set(Objects.requireNonNullElse(tenant, "unknown"));
    }

    public void clearCurrentTenant() {
        currentTenant.remove();
    }
}
