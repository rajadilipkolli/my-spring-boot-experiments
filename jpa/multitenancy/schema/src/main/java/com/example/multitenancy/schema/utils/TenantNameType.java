package com.example.multitenancy.schema.utils;


public enum TenantNameType {
    TEST1("test1"),
    TEST2("test2");

    private final String tenantName;

    TenantNameType(String tenantName) {
        this.tenantName = tenantName;
    }
}
