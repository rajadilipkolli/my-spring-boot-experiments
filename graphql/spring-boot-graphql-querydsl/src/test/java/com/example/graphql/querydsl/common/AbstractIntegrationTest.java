package com.example.graphql.querydsl.common;

import static com.example.graphql.querydsl.utils.AppConstants.PROFILE_IT;
import static com.example.graphql.querydsl.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({PROFILE_TEST, PROFILE_IT})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ImportTestcontainers(DBContainerInitializerBase.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;
}
