package com.example.mongoes.mongodb.config;

import com.example.mongoes.mongodb.eventlistener.CascadeSaveMongoEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.example.mongoes.mongodb.repository")
public class MongoDBConfig {

  @Bean
  public CascadeSaveMongoEventListener cascadeSaveMongoEventListener(
      MongoOperations mongoOperations,
      ReactiveElasticsearchOperations reactiveElasticsearchOperations,
      ObjectMapper objectMapper) {
    return new CascadeSaveMongoEventListener(
        mongoOperations, reactiveElasticsearchOperations, objectMapper);
  }

  @Bean
  AuditorAware<String> auditorAware() {
    return () -> Optional.of("application");
  }
}
