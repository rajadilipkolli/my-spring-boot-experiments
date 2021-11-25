package com.example.mongoes.mongodb.config;

import com.example.mongoes.mongodb.eventlistener.CascadeSaveMongoEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class MongoDBConfig {

  private final MongoOperations mongoOperations;

  @Bean
  public CascadeSaveMongoEventListener cascadeSaveMongoEventListener() {
    return new CascadeSaveMongoEventListener(mongoOperations);
  }
}
