package com.example.multipledatasources.configuration;

import com.example.multipledatasources.entities.cardholder.CardHolder;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
        basePackageClasses = CardHolderRepository.class,
        entityManagerFactoryRef = "cardHolderEntityManagerFactory",
        transactionManagerRef = "cardHolderTransactionManager")
class CardHolderDataSourceConfiguration {

    @Qualifier("mysql") @Bean(defaultCandidate = false)
    @ConfigurationProperties("app.datasource.cardholder")
    DataSourceProperties cardHolderDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Qualifier("mysql") @Bean(defaultCandidate = false)
    @ConfigurationProperties("app.datasource.cardholder.hikari")
    DataSource cardholderDataSource(@Qualifier("mysql") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Qualifier("mysql") @Bean(defaultCandidate = false)
    LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory(
            @Qualifier("mysql") DataSource dataSource, EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource)
                .packages(CardHolder.class)
                .persistenceUnit("cardholder")
                .build();
    }

    @Qualifier("mysql") @Bean(defaultCandidate = false)
    PlatformTransactionManager cardHolderTransactionManager(
            @Qualifier("mysql") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
