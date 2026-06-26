package com.example.custom.sequence.config.db;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.observation.boot.autoconfigure.ProxyDataSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        repositoryBaseClass = BaseJpaRepositoryImpl.class,
        basePackages = "com.example.custom.sequence.repositories")
public class JpaConfig {

    @Bean
    ProxyDataSourceBuilderCustomizer myCustomizer() {
        return (builder, dataSource, beanName, dataSourceName) -> {
            builder.listener(new DataSourceQueryCountListener());
            builder.name("customSeqDsProxy");
        };
    }
}
