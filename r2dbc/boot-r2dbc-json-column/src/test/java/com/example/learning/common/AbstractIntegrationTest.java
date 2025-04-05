package com.example.learning.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainerConfig.class)
@AutoConfigureWebTestClient
@AutoConfigureObservability // enable tracing in test
public abstract class AbstractIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
}
