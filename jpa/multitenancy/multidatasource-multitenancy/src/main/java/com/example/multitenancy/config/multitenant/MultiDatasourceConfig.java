package com.example.multitenancy.config.multitenant;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class MultiDatasourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("datasource.primary")
    DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("datasource.primary.configuration")
    DataSource primaryDataSource(DataSourceProperties primaryDataSourceProperties) {
        return primaryDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource.primary.liquibase")
    LiquibaseProperties primaryLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    SpringLiquibase primaryLiquibase(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("primaryLiquibaseProperties")
                    LiquibaseProperties primaryLiquibaseProperties) {
        return springLiquibase(primaryDataSource, primaryLiquibaseProperties);
    }

    @Bean
    @ConfigurationProperties("datasource.secondary")
    DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("datasource.secondary.hikari")
    DataSource secondaryDataSource(
            @Qualifier("secondaryDataSourceProperties")
                    DataSourceProperties secondaryDataSourceProperties) {
        return secondaryDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource.secondary.liquibase")
    LiquibaseProperties secondaryLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    SpringLiquibase secondaryLiquibase(
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource,
            @Qualifier("secondaryLiquibaseProperties")
                    LiquibaseProperties secondaryLiquibaseProperties) {
        return springLiquibase(secondaryDataSource, secondaryLiquibaseProperties);
    }

    // Copied from LiquibaseAutoConfiguration class
    private SpringLiquibase springLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setClearCheckSums(properties.isClearChecksums());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setLabelFilter(properties.getLabelFilter());
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
        liquibase.setTag(properties.getTag());
        return liquibase;
    }
}
