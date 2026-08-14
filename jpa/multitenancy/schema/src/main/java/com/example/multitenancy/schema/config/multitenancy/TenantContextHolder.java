package com.example.multitenancy.schema.config.multitenancy;

/**
 * Holder for the {@link ScopedValue} representing the current tenant identifier.
 *
 * This class centralises the tenant context and provides a single public static
 * final instance that can be bound using {@code ScopedValue.where(...)}. The field
 * is deliberately public to allow convenient static import, but its location in a
 * dedicated holder class reduces coupling compared to being defined directly in a
 * filter implementation.
 */
public final class TenantContextHolder {
    private TenantContextHolder() {
        // Prevent instantiation
    }

    /** Scoped value that holds the current tenant identifier for the request. */
    public static final ScopedValue<String> CURRENT_TENANT = ScopedValue.newInstance();
}
