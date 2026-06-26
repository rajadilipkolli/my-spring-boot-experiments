package com.example.demo.readreplica;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReadReplicaApplication {

    public static void main(String[] args) {
        // Set default timezone to UTC to avoid timezone conflicts
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ReadReplicaApplication.class, args);
    }
}
