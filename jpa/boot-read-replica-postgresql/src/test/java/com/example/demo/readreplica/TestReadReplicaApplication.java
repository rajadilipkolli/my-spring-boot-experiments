package com.example.demo.readreplica;

import com.example.demo.readreplica.common.ContainersConfiguration;
import org.springframework.boot.SpringApplication;

class TestReadReplicaApplication {

    public static void main(String[] args) {
        SpringApplication.from(ReadReplicaApplication::main)
                .with(ContainersConfiguration.class)
                .run(args);
    }
}
