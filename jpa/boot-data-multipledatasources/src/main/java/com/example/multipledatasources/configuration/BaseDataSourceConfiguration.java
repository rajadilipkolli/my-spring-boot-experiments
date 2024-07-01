package com.example.multipledatasources.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class BaseDataSourceConfiguration {

    protected final PersistenceUnitManager persistenceUnitManager;
    protected final JpaProperties jpaProperties;

    protected BaseDataSourceConfiguration(
            ObjectProvider<PersistenceUnitManager> persistenceUnitManager, JpaProperties jpaProperties) {
        this.persistenceUnitManager = persistenceUnitManager.getIfAvailable();
        this.jpaProperties = jpaProperties;
    }

    protected EntityManagerFactoryBuilder createEntityManagerFactoryBuilder() {
        JpaVendorAdapter jpaVendorAdapter = createJpaVendorAdapter();
        return new EntityManagerFactoryBuilder(
                jpaVendorAdapter, jpaProperties.getProperties(), this.persistenceUnitManager);
    }

    protected JpaVendorAdapter createJpaVendorAdapter() {
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

    protected PlatformTransactionManager createTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
