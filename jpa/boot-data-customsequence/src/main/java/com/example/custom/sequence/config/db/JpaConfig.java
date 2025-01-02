package com.example.custom.sequence.config.db;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        repositoryBaseClass = BaseJpaRepositoryImpl.class,
        basePackages = "com.example.custom.sequence.repositories")
public class JpaConfig {}
