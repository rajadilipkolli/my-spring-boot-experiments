package com.example.multitenancy.config.multitenant;

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

    /**
     * ThreadLocal fallback for contexts where ScopedValue cannot be used (e.g. tests,
     * scheduled tasks). Takes lower priority than {@link #CURRENT_TENANT} when both are set.
     */
    public static final ThreadLocal<String> CURRENT_TENANT_THREAD_LOCAL = new ThreadLocal<>();
}
