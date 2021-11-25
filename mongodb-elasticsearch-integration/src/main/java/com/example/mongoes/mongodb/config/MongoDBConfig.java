package com.example.mongoes.mongodb.config;

import com.example.mongoes.elasticsearch.repository.ERestaurantRepository;
import com.example.mongoes.mongodb.eventlistener.CascadeSaveMongoEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.example.mongoes.mongodb.repository")
public class MongoDBConfig {

  private final MongoOperations mongoOperations;
  private final ERestaurantRepository eRestaurantRepository;

  @Bean
  public CascadeSaveMongoEventListener cascadeSaveMongoEventListener() {
    return new CascadeSaveMongoEventListener(mongoOperations, eRestaurantRepository);
  }

  @Bean
  AuditorAware<String> auditorAware() {
    return () -> Optional.of("application");
  }
}
