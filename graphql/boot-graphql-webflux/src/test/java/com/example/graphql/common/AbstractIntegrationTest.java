package com.example.graphql.common;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContainerConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;
}
