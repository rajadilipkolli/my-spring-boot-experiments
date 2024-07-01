package com.example.multipledatasources.configuration;

import com.example.multipledatasources.model.cardholder.CardHolder;
import com.example.multipledatasources.repository.cardholder.CardHolderRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = CardHolderRepository.class,
        entityManagerFactoryRef = "cardHolderEntityManagerFactory",
        transactionManagerRef = "cardHolderTransactionManager")
public class CardHolderDataSourceConfiguration {

    private final PersistenceUnitManager persistenceUnitManager;

    public CardHolderDataSourceConfiguration(ObjectProvider<PersistenceUnitManager> persistenceUnitManager) {
        this.persistenceUnitManager = persistenceUnitManager.getIfAvailable();
    }

    @Bean
    @ConfigurationProperties("app.datasource.cardholder.jpa")
    JpaProperties cardHolderJpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.cardholder")
    DataSourceProperties cardHolderDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.cardholder.hikari")
    DataSource cardholderDataSource() {
        return cardHolderDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory(
            JpaProperties cardHolderJpaProperties, DataSource cardholderDataSource) {
        EntityManagerFactoryBuilder builder = createEntityManagerFactoryBuilder(cardHolderJpaProperties);
        return builder.dataSource(cardholderDataSource)
                .packages(CardHolder.class)
                .build();
    }

    @Bean
    PlatformTransactionManager cardHolderTransactionManager(EntityManagerFactory cardHolderEntityManagerFactory) {
        return new JpaTransactionManager(cardHolderEntityManagerFactory);
    }

    private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(JpaProperties cardHolderJpaProperties) {
        JpaVendorAdapter jpaVendorAdapter = createJpaVendorAdapter(cardHolderJpaProperties);
        return new EntityManagerFactoryBuilder(
                jpaVendorAdapter, cardHolderJpaProperties.getProperties(), this.persistenceUnitManager);
    }

    private JpaVendorAdapter createJpaVendorAdapter(JpaProperties jpaProperties) {
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        if (jpaProperties.getDatabase() != null) {
            adapter.setDatabase(jpaProperties.getDatabase());
        }
        if (jpaProperties.getDatabasePlatform() != null) {
            adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
        }
        adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        return adapter;
    }
}
