package com.example.envers.common;

import static com.example.envers.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ImportTestcontainers(DBContainerInitializer.class)
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;
}
