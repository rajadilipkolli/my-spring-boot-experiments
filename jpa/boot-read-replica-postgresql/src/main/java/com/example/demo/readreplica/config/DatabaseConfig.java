package com.example.demo.readreplica.config;

import com.example.demo.readreplica.config.routing.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfig {

    private static final String PRIMARY_DATABASE_PROPERTY_KEY_PREFIX = "spring.primary.datasource";
    private static final String REPLICA_DATABASE_PROPERTY_KEY_PREFIX = "spring.replica.datasource";

    @Bean
    @ConfigurationProperties(PRIMARY_DATABASE_PROPERTY_KEY_PREFIX)
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(PRIMARY_DATABASE_PROPERTY_KEY_PREFIX + ".configuration")
    public DataSource primaryDataSource(final DataSourceProperties primaryDataSourceProperties) {
        return primaryDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(REPLICA_DATABASE_PROPERTY_KEY_PREFIX)
    public DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(REPLICA_DATABASE_PROPERTY_KEY_PREFIX + ".configuration")
    public DataSource replicaDataSource(final DataSourceProperties replicaDataSourceProperties) {
        return replicaDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public DataSource dataSource(
            final DataSource primaryDataSource, final DataSource replicaDataSource) {
        final RoutingDataSource routingDataSource = new RoutingDataSource();

        final Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(RoutingDataSource.Route.PRIMARY, primaryDataSource);
        targetDataSources.put(RoutingDataSource.Route.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);

        return routingDataSource;
    }
}
