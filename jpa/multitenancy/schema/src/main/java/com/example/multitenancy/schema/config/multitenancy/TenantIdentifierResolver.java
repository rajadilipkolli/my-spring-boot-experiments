package com.example.multitenancy.schema.config.multitenancy;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private static final String UNKNOWN = "unknown";
    private static final ScopedValue<String> CURRENT_TENANT = ScopedValue.newInstance();

    public void runWithTenant(@Nullable String tenant, Runnable action) {
        ScopedValue.where(CURRENT_TENANT, normalize(tenant)).run(action);
    }

    public <T> T withTenant(@Nullable String tenant, Supplier<T> supplier) {
        return ScopedValue.where(CURRENT_TENANT, normalize(tenant)).call(supplier::get);
    }

    public <T, X extends Exception> T callWithTenant(
            @Nullable String tenant, ScopedValue.CallableOp<? extends T, X> callable) throws X {
        return ScopedValue.where(CURRENT_TENANT, normalize(tenant)).call(callable);
    }

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

    private String normalize(@Nullable String tenant) {
        return Objects.requireNonNullElse(tenant, UNKNOWN);
    }
}
