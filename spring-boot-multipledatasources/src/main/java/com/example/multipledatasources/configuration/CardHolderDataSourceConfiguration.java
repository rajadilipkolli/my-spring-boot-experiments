package com.example.multipledatasources.configuration;

import com.example.multipledatasources.model.cardholder.CardHolder;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.multipledatasources.repository.cardholder",
        entityManagerFactoryRef = "cardHolderEntityManagerFactory",
        transactionManagerRef = "cardHolderTransactionManager")
public class CardHolderDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("app.datasource.cardholder")
    public DataSourceProperties cardHolderDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.cardholder.hikari")
    public DataSource cardholderDataSource() {
        return cardHolderDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "cardHolderEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(cardholderDataSource()).packages(CardHolder.class).build();
    }

    @Bean
    public PlatformTransactionManager cardHolderTransactionManager(
            final @Qualifier("cardHolderEntityManagerFactory")
                    LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory) {
        return new JpaTransactionManager(cardHolderEntityManagerFactory.getObject());
    }
}
