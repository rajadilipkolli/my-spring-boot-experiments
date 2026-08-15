package com.example.multitenancy.config.multitenant;

import java.util.Map;
import java.util.Objects;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component("tenantIdentifierResolver")
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    public void setCurrentTenant(@Nullable String tenant) {
        TenantContextHolder.CURRENT_TENANT_THREAD_LOCAL.set(Objects.requireNonNullElse(tenant, "unknown"));
    }

    public void clearCurrentTenant() {
        TenantContextHolder.CURRENT_TENANT_THREAD_LOCAL.remove();
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        if (TenantContextHolder.CURRENT_TENANT.isBound()) {
            String tenant = TenantContextHolder.CURRENT_TENANT.get();
            return tenant != null ? tenant : "unknown";
        }
        String tenant = TenantContextHolder.CURRENT_TENANT_THREAD_LOCAL.get();
        return tenant != null ? tenant : "unknown";
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
