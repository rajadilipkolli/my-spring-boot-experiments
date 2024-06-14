package com.example.multitenancy.schema.utils;

import lombok.Getter;

@Getter
public enum TenantNameType {
    TEST1("test1"),
    TEST2("test2");

    private final String tenantName;

    TenantNameType(String tenantName) {
        this.tenantName = tenantName;
    }
}
