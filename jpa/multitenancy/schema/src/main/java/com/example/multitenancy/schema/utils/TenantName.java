package com.example.multitenancy.schema.utils;

public enum TenantName {
    TEST1("test1"),
    TEST2("test2");

    public final String tenantName;

    private TenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}
