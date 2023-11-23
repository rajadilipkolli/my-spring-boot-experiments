package com.example.graphql.common;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;

import com.example.graphql.TestApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestApplication.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;
}
