package com.example.graphql.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.graphql.utils.AppConstants.PROFILE_IT;
import static com.example.graphql.utils.AppConstants.PROFILE_TEST;

@ActiveProfiles({PROFILE_TEST, PROFILE_IT})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest extends DBContainerInitializerBase {

    @Autowired protected ObjectMapper objectMapper;
}
