package com.example.featuretoggle.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Configuration(proxyBeanMethods = false)
public class DatabaseTogglzConfiguration {

    private final DataSource dataSource;

    public DatabaseTogglzConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    StateRepository jdbcStateRepository() {
        return JDBCStateRepository.newBuilder(dataSource).createTable(false).build();
    }

    @Bean
    UserProvider getUserProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }
}
