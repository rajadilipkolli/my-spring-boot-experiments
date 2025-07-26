package com.example.ultimateredis.utils;

public final class AppConstants {
    public static final String PROFILE_SENTINEL = "sentinel";
    public static final String PROFILE_CLUSTER = "cluster";
    public static final String PROFILE_NOT_CLUSTER = "!" + PROFILE_CLUSTER;
    public static final String PROFILE_STANDALONE = "standalone";

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
