package com.example.graphql.utils;

public final class AppConstants {
    public static final String PROFILE_LOCAL = "local";
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_NOT_PROD = "!" + PROFILE_PROD;
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_IT = "integration-test";

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
