package com.example.demo.readreplica.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration(proxyBeanMethods = false)
class DatabaseConfig {

    private static final String PRIMARY_DATABASE_PROPERTY_KEY_PREFIX = "spring.primary.datasource";
    private static final String REPLICA_DATABASE_PROPERTY_KEY_PREFIX = "spring.replica.datasource";

    @Bean
    @ConfigurationProperties(PRIMARY_DATABASE_PROPERTY_KEY_PREFIX)
    DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(PRIMARY_DATABASE_PROPERTY_KEY_PREFIX + ".hikari")
    DataSource primaryDataSource(final DataSourceProperties primaryDataSourceProperties) {
        return primaryDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(REPLICA_DATABASE_PROPERTY_KEY_PREFIX)
    DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(REPLICA_DATABASE_PROPERTY_KEY_PREFIX + ".hikari")
    DataSource replicaDataSource(final DataSourceProperties replicaDataSourceProperties) {
        return replicaDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    DataSource dataSource(final DataSource primaryDataSource, final DataSource replicaDataSource) {
        LazyConnectionDataSourceProxy lazyConnectionDataSourceProxy =
                new LazyConnectionDataSourceProxy(primaryDataSource);
        lazyConnectionDataSourceProxy.setReadOnlyDataSource(replicaDataSource);
        return lazyConnectionDataSourceProxy;
    }
}
