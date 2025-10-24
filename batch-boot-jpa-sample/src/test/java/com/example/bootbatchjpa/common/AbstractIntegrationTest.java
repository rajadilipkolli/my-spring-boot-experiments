package com.example.bootbatchjpa.common;

import static com.example.bootbatchjpa.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {ContainersConfig.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected JsonMapper jsonMapper;
}
