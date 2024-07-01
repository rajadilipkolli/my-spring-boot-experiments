package com.example.multipledatasources.configuration;

import com.example.multipledatasources.model.member.Member;
import com.example.multipledatasources.repository.member.MemberRepository;
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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackageClasses = MemberRepository.class,
        entityManagerFactoryRef = "memberEntityManagerFactory",
        transactionManagerRef = "memberTransactionManager")
public class MemberDataSourceConfiguration {

    private final PersistenceUnitManager persistenceUnitManager;
    private final JpaProperties jpaProperties;

    public MemberDataSourceConfiguration(
            ObjectProvider<PersistenceUnitManager> persistenceUnitManager, JpaProperties jpaProperties) {
        this.persistenceUnitManager = persistenceUnitManager.getIfAvailable();
        this.jpaProperties = jpaProperties;
    }

    @Bean
    @ConfigurationProperties("app.datasource.member")
    DataSourceProperties memberDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.member.configuration")
    DataSource memberDataSource() {
        return memberDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean memberEntityManagerFactory(DataSource memberDataSource) {
        EntityManagerFactoryBuilder builder = createEntityManagerFactoryBuilder();
        return builder.dataSource(memberDataSource).packages(Member.class).build();
    }

    @Primary
    @Bean
    PlatformTransactionManager memberTransactionManager(EntityManagerFactory memberEntityManagerFactory) {
        return new JpaTransactionManager(memberEntityManagerFactory);
    }

    private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder() {
        JpaVendorAdapter jpaVendorAdapter = createJpaVendorAdapter();
        return new EntityManagerFactoryBuilder(
                jpaVendorAdapter, jpaProperties.getProperties(), this.persistenceUnitManager);
    }

    private JpaVendorAdapter createJpaVendorAdapter() {
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
