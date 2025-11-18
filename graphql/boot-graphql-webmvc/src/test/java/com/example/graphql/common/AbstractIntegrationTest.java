package com.example.graphql.common;

import static com.example.graphql.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.graphql.TestGraphQlWebMvcApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestGraphQlWebMvcApplication.class)
@AutoConfigureMockMvc
@AutoConfigureHttpGraphQlTester
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected HttpGraphQlTester graphQlTester;
}
