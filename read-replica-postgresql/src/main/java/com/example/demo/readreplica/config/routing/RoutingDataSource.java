package com.example.demo.readreplica.config.routing;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<Route> routeContext = new ThreadLocal<>();

    public enum Route {
        PRIMARY, REPLICA
    }

    public static void clearReplicaRoute() {
        routeContext.remove();
    }

    public static void setReplicaRoute() {
        routeContext.set(Route.REPLICA);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return routeContext.get();
    }
}
