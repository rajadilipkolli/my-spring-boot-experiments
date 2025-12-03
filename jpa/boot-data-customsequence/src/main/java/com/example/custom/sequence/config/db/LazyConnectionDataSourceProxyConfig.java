package com.example.custom.sequence.config.db;

import io.hypersistence.utils.logging.InlineQueryLogEntryCreator;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

/**
 * Configuration class that sets up a lazy-loading data source proxy to improve performance by
 * deferring physical database connections until they are actually needed. This optimization is
 * particularly useful in scenarios where database operations don't always occur during request
 * processing.
 */
@AutoConfiguration(
        after = {DataSourceAutoConfiguration.class},
        before = {HibernateJpaAutoConfiguration.class})
class LazyConnectionDataSourceProxyConfig {

    private static final String DATA_SOURCE_PROXY_NAME = "customSeqDsProxy";

    @Bean
    @Primary
    DataSource lazyConnectionDataSourceProxy(DataSource dataSource) {
        // Add listeners for query logging
        ChainListener listener = new ChainListener();
        SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
        loggingListener.setQueryLogEntryCreator(new InlineQueryLogEntryCreator());
        listener.addListener(loggingListener);
        listener.addListener(new DataSourceQueryCountListener());

        // Wrap with ProxyDataSource and LazyConnectionDataSourceProxy
        DataSource loggingDataSource = ProxyDataSourceBuilder.create(dataSource)
                .name(DATA_SOURCE_PROXY_NAME)
                .listener(listener)
                .build();

        return new LazyConnectionDataSourceProxy(loggingDataSource);
    }
}
