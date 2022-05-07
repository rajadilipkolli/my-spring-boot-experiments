package com.example.envers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;

@Configuration(proxyBeanMethods = false)
@EnableEnversRepositories(basePackages = "com.example.envers.repositories")
public class RepositoryConfiguration {}
