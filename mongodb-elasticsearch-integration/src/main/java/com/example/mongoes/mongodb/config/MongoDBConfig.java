package com.example.mongoes.mongodb.config;

import com.example.mongoes.mongodb.event.CascadeSaveMongoEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class MongoDBConfig {

    private final ReactiveMongoOperations reactiveMongoOperations;

    @Bean
    public CascadeSaveMongoEventListener cascadeSaveMongoEventListener() {
        return new CascadeSaveMongoEventListener(reactiveMongoOperations);
    }
}
