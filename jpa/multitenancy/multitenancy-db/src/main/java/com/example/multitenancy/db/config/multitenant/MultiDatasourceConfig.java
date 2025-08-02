package com.example.multitenancy.db.config.multitenant;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.UIServiceEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
class MultiDatasourceConfig {

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
            @Qualifier("primaryLiquibaseProperties") LiquibaseProperties primaryLiquibaseProperties) {
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
            @Qualifier("secondaryDataSourceProperties") DataSourceProperties secondaryDataSourceProperties) {
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
            @Qualifier("secondaryLiquibaseProperties") LiquibaseProperties secondaryLiquibaseProperties) {
        return springLiquibase(secondaryDataSource, secondaryLiquibaseProperties);
    }

    // Copied from LiquibaseAutoConfiguration class
    private SpringLiquibase springLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setClearCheckSums(properties.isClearChecksums());
        if (!CollectionUtils.isEmpty(properties.getContexts())) {
            liquibase.setContexts(StringUtils.collectionToCommaDelimitedString(properties.getContexts()));
        }
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        if (!CollectionUtils.isEmpty(properties.getLabelFilter())) {
            liquibase.setLabelFilter(StringUtils.collectionToCommaDelimitedString(properties.getLabelFilter()));
        }
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
        liquibase.setTag(properties.getTag());
        if (properties.getShowSummary() != null) {
            liquibase.setShowSummary(
                    UpdateSummaryEnum.valueOf(properties.getShowSummary().name()));
        }
        if (properties.getShowSummaryOutput() != null) {
            liquibase.setShowSummaryOutput(UpdateSummaryOutputEnum.valueOf(
                    properties.getShowSummaryOutput().name()));
        }
        if (properties.getUiService() != null) {
            liquibase.setUiService(
                    UIServiceEnum.valueOf(properties.getUiService().name()));
        }
        return liquibase;
    }
}
