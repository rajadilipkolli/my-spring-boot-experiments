package com.example.multitenancy.config.multidatasource;

import com.example.multitenancy.config.multitenant.TenantIdentifierResolver;
import com.example.multitenancy.secondary.entities.SecondaryCustomer;
import com.example.multitenancy.secondary.repositories.SecondaryCustomerRepository;
import com.example.multitenancy.utils.DatabaseType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = SecondaryCustomer.class)
@EnableJpaRepositories(
        basePackageClasses = SecondaryCustomerRepository.class,
        entityManagerFactoryRef = "secondaryEntityManagerFactory",
        transactionManagerRef = "secondaryTransactionManager")
public class SecondaryDataSourceConfiguration {

    private final JpaProperties jpaProperties;
    private final TenantIdentifierResolver tenantIdentifierResolver;

    public SecondaryDataSourceConfiguration(
            JpaProperties jpaProperties, TenantIdentifierResolver tenantIdentifierResolver) {
        this.jpaProperties = jpaProperties;
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Bean(name = "secondaryEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("tenantRoutingDatasource") DataSource tenantRoutingDatasource,
            @Qualifier("multiTenantConnectionProviderImpl") MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
            @Qualifier("tenantIdentifierResolver") CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver) {
        Map<String, Object> hibernateProps = new HashMap<>();
        hibernateProps.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        hibernateProps.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        hibernateProps.putAll(this.jpaProperties.getProperties());
        // needs to set tenantIdentifier for connecting to secondary datasource and fetching the
        // metadata
        tenantIdentifierResolver.setCurrentTenant(DatabaseType.SECONDARY.getSchemaName());
        return builder.dataSource(tenantRoutingDatasource)
                .properties(hibernateProps)
                .persistenceUnit("secondary")
                .packages(SecondaryCustomer.class)
                .build();
    }

    @Bean
    PlatformTransactionManager secondaryTransactionManager(
            final @Qualifier("secondaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean
                            secondaryEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(secondaryEntityManagerFactory.getObject()));
    }
}
