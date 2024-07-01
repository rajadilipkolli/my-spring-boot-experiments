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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = CardHolderRepository.class,
        entityManagerFactoryRef = "cardHolderEntityManagerFactory",
        transactionManagerRef = "cardHolderTransactionManager")
public class CardHolderDataSourceConfiguration extends BaseDataSourceConfiguration {

    public CardHolderDataSourceConfiguration(
            ObjectProvider<PersistenceUnitManager> persistenceUnitManager, JpaProperties jpaProperties) {
        super(persistenceUnitManager, jpaProperties);
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
    LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory(DataSource cardholderDataSource) {
        EntityManagerFactoryBuilder builder = createEntityManagerFactoryBuilder();
        return builder.dataSource(cardholderDataSource)
                .packages(CardHolder.class)
                .build();
    }

    @Bean
    PlatformTransactionManager cardHolderTransactionManager(EntityManagerFactory cardHolderEntityManagerFactory) {
        return createTransactionManager(cardHolderEntityManagerFactory);
    }
}
