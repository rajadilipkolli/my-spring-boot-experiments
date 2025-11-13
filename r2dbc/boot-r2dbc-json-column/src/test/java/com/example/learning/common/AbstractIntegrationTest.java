package com.example.learning.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainerConfig.class)
@AutoConfigureWebTestClient
@AutoConfigureTracing // enable tracing in test
public abstract class AbstractIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
}
