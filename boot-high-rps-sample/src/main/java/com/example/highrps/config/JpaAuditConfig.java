package com.example.highrps.config;

import com.example.highrps.repository.jpa.AuthorRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// As JpaAuditing works based on Proxy we shouldn't create configuration as proxyBeans as false
@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
@EnableJpaRepositories(basePackageClasses = AuthorRepository.class)
public class JpaAuditConfig {}
