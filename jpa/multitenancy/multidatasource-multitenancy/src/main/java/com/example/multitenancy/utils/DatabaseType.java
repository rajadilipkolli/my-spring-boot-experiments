package com.example.multitenancy.utils;


public enum DatabaseType {
    PRIMARY("primary"),
    SECONDARY("secondary"),
    SCHEMA1("schema1"),
    SCHEMA2("schema2"),
    DBSYSTC("dbsystc"),
    DBSYSTP("dbsystp"),
    DBSYSTV("dbsystv");

    private final String schemaName;

    DatabaseType(String schemaName) {
        this.schemaName = schemaName;
    }
}
