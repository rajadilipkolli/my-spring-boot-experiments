package com.example.rest.template.utils;


public final class AppConstants {
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_NOT_PROD = "!" + PROFILE_PROD;
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_NOT_TEST = "!" + PROFILE_TEST;
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Connection pool
    public static final int MAX_ROUTE_CONNECTIONS = 40;
    public static final int MAX_TOTAL_CONNECTIONS = 40;
    public static final int MAX_LOCALHOST_CONNECTIONS = 80;

    // Keep alive
    public static final int DEFAULT_KEEP_ALIVE_TIME = 20; // 20 sec

    // Timeouts
    public static final int CONNECTION_TIMEOUT =
            30; // 30 sec, the time for waiting until a connection is established
    public static final int REQUEST_TIMEOUT =
            30; // 30 sec, the time for waiting for a connection from connection pool
    public static final int SOCKET_TIMEOUT = 60; // 60 sec, the time for waiting for data

    // Idle connection monitor
    public static final int IDLE_CONNECTION_WAIT_TIME = 30; // 30 sec
}
