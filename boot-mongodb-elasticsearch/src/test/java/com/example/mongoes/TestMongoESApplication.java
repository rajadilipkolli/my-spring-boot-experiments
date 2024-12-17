package com.example.mongoes;

import com.example.mongoes.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestMongoESApplication {

    public static void main(String[] args) {
        SpringApplication.from(MongoESApplication::main).with(ContainersConfig.class).run(args);
    }
}
