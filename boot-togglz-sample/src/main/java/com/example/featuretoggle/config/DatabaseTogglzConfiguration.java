package com.example.featuretoggle.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class DatabaseTogglzConfiguration {

    private final DataSource dataSource;

    @Bean
    StateRepository jdbcStateRepository() {
        return JDBCStateRepository.newBuilder(dataSource).createTable(false).build();
    }

    @Bean
    UserProvider getUserProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }
}
