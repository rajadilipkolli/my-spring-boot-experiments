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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = MemberRepository.class,
        entityManagerFactoryRef = "memberEntityManagerFactory",
        transactionManagerRef = "memberTransactionManager")
public class MemberDataSourceConfiguration extends BaseDataSourceConfiguration {

    public MemberDataSourceConfiguration(
            ObjectProvider<PersistenceUnitManager> persistenceUnitManager, JpaProperties jpaProperties) {
        super(persistenceUnitManager, jpaProperties);
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
        return createTransactionManager(memberEntityManagerFactory);
    }
}
