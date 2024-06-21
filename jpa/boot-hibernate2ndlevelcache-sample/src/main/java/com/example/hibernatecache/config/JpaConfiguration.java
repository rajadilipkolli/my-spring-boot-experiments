package com.example.hibernatecache.config;

import com.example.hibernatecache.repositories.CustomerRepository;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(
        basePackageClasses = CustomerRepository.class,
        repositoryBaseClass = BaseJpaRepositoryImpl.class)
@Configuration(proxyBeanMethods = false)
class JpaConfiguration {}
