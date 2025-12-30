package com.example.jooq.r2dbc.common;

import static com.example.jooq.r2dbc.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainerConfig.class)
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    @Autowired protected WebTestClient webTestClient;
}
