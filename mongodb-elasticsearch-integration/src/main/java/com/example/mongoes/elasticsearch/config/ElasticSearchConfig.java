package com.example.mongoes.elasticsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@Configuration(proxyBeanMethods = false)
@EnableReactiveElasticsearchRepositories(
    basePackages = "com.example.mongoes.elasticsearch.repository")
public class ElasticSearchConfig {}
