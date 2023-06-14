package com.example.multitenancy.config.multidatasource;

import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.multitenancy.secondary.repositories",
        entityManagerFactoryRef = "secondaryEntityManagerFactory",
        transactionManagerRef = "secondaryTransactionManager")
@RequiredArgsConstructor
public class SecondaryDataSourceConfiguration {

    private final JpaProperties jpaProperties;

    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("tenantRoutingDatasource") DataSource tenantRoutingDatasource,
            @Qualifier("multiTenantConnectionProviderImpl")
                    MultiTenantConnectionProvider multiTenantConnectionProvider,
            @Qualifier("tenantIdentifierResolver")
                    CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
        Map<String, Object> hibernateProps = new HashMap<>();
        hibernateProps.put(
                AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        hibernateProps.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                currentTenantIdentifierResolver);
        hibernateProps.putAll(this.jpaProperties.getProperties());
        return builder.dataSource(tenantRoutingDatasource)
                .properties(hibernateProps)
                .persistenceUnit("secondary")
                .packages(SecondaryCustomer.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            final @Qualifier("secondaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean
                            secondaryEntityManagerFactory) {
        return new JpaTransactionManager(
                Objects.requireNonNull(secondaryEntityManagerFactory.getObject()));
    }
}
