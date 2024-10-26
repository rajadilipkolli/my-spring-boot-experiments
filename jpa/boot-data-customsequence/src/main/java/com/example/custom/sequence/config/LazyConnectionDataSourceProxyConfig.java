package com.example.custom.sequence.config;

import com.zaxxer.hikari.HikariDataSource;
import io.hypersistence.utils.logging.InlineQueryLogEntryCreator;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.util.StringUtils;

/**
 * Configuration class that sets up a lazy-loading data source proxy to improve performance by
 * deferring physical database connections until they are actually needed. This optimization is
 * particularly useful in scenarios where database operations don't always occur during request
 * processing.
 */
@Configuration(proxyBeanMethods = false)
class LazyConnectionDataSourceProxyConfig {

    private static final String DATA_SOURCE_PROXY_NAME = "customSeqDsProxy";
    private static final Logger log =
            LoggerFactory.getLogger(LazyConnectionDataSourceProxyConfig.class);

    private final DataSourceProperties dataSourceProperties;

    LazyConnectionDataSourceProxyConfig(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    HikariDataSource hikariDataSource(ConfigurableListableBeanFactory beanFactory) {
        JdbcConnectionDetails connectionDetails;
        try {
            // Try to get JdbcConnectionDetails bean if available
            connectionDetails = beanFactory.getBean(JdbcConnectionDetails.class);
            dataSourceProperties.setDriverClassName(connectionDetails.getDriverClassName());
            dataSourceProperties.setUrl(connectionDetails.getJdbcUrl());
            dataSourceProperties.setUsername(connectionDetails.getUsername());
            dataSourceProperties.setPassword(connectionDetails.getPassword());
        } catch (NoSuchBeanDefinitionException e) {
            // Ignore as JdbcConnectionDetails might not be available in non-test environments
            log.debug("JdbcConnectionDetails bean not found, falling back to properties", e);
        }

        // Create and configure HikariDataSource with properties
        HikariDataSource hikariDataSource =
                dataSourceProperties
                        .initializeDataSourceBuilder()
                        .type(HikariDataSource.class)
                        .build();

        if (StringUtils.hasText(dataSourceProperties.getName())) {
            hikariDataSource.setPoolName(dataSourceProperties.getName());
        }

        return hikariDataSource;
    }

    @Bean
    @Primary
    DataSource dataSource(HikariDataSource hikariDataSource) {
        // Add listeners for query logging
        ChainListener listener = new ChainListener();
        SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
        loggingListener.setQueryLogEntryCreator(new InlineQueryLogEntryCreator());
        listener.addListener(loggingListener);
        listener.addListener(new DataSourceQueryCountListener());

        // Wrap with ProxyDataSource and LazyConnectionDataSourceProxy
        DataSource loggingDataSource =
                ProxyDataSourceBuilder.create(hikariDataSource)
                        .name(DATA_SOURCE_PROXY_NAME)
                        .listener(listener)
                        .build();

        return new LazyConnectionDataSourceProxy(loggingDataSource);
    }
}
