package com.example.multitenancy.config.multidatasource;

import com.example.multitenancy.config.multitenant.TenantIdentifierResolver;
import com.example.multitenancy.primary.entities.PrimaryCustomer;
import com.example.multitenancy.utils.DatabaseType;
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
        basePackages = "com.example.multitenancy.primary.repositories",
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager")
@RequiredArgsConstructor
public class PrimaryDataSourceConfiguration {

    private final JpaProperties jpaProperties;
    private final TenantIdentifierResolver tenantIdentifierResolver;

    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
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
        // needs to set tenantIdentifier for connecting to primary datasource and fetching the
        // metadata
        tenantIdentifierResolver.setCurrentTenant(DatabaseType.primary.name());
        return builder.dataSource(tenantRoutingDatasource)
                .persistenceUnit("primary")
                .properties(hibernateProps)
                .packages(PrimaryCustomer.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager primaryTransactionManager(
            final @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean
                            primaryEntityManagerFactory) {
        return new JpaTransactionManager(
                Objects.requireNonNull(primaryEntityManagerFactory.getObject()));
    }
}
