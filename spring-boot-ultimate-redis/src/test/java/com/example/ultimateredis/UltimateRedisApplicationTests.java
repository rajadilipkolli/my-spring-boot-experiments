package com.example.ultimateredis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class UltimateRedisApplicationTests {

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer redisContainer =
            new GenericContainer(DockerImageName.parse("redis").withTag("7.0.11"));

    @Test
    void contextLoads() {}
}
