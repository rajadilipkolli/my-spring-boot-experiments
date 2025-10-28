package com.example.demo.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainerConfig.class)
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
}
