package com.example.highrps.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// As JpaAuditing works based on Proxy we shouldn't create configuration as proxyBeans as false
@Configuration
@EnableJpaAuditing(modifyOnCreate = false)
public class JpaAuditConfig {}
