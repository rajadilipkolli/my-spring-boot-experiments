package com.example.hibernatecache.config;

import com.example.hibernatecache.repositories.CustomerRepository;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackageClasses = CustomerRepository.class, repositoryBaseClass = BaseJpaRepositoryImpl.class)
@Configuration(proxyBeanMethods = false)
class JpaConfiguration {

    @Bean
    QueryExecutionListener queryExecutionListener() {
        return new DataSourceQueryCountListener();
    }
}
