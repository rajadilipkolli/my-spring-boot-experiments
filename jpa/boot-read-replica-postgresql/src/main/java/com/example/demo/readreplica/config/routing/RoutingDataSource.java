package com.example.demo.readreplica.config.routing;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<Route> routeContext = new ThreadLocal<>();

    public enum Route {
        PRIMARY,
        REPLICA
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

    public boolean isWrapperFor(Class<?> iface) throws java.sql.SQLException {
        // TODO Auto-generated method stub
        return iface != null && iface.isAssignableFrom(this.getClass());
    }

    public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
        // TODO Auto-generated method stub
        try {
            if (iface != null && iface.isAssignableFrom(this.getClass())) {
                return (T) this;
            }
            throw new java.sql.SQLException("Auto-generated unwrap failed; Revisit implementation");
        } catch (Exception e) {
            throw new java.sql.SQLException(e);
        }
    }

    public java.util.logging.Logger getParentLogger() {
        // TODO Auto-generated method stub
        return null;
    }
}
