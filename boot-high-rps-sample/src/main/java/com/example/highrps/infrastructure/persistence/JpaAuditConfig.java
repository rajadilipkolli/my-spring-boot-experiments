package com.example.highrps.infrastructure.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
@EnableJpaRepositories(basePackages = "com.example.highrps")
public class JpaAuditConfig {}
