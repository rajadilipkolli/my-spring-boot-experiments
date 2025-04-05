package com.example.keysetpagination.common;

import static com.example.keysetpagination.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests providing common test utilities and configurations.
 * <p>
 * This class sets up the test environment with:
 * <ul>
 *   <li>Random port web environment to avoid conflicts</li>
 *   <li>Mock MVC for testing REST endpoints</li>
 *   <li>Object mapper for JSON serialization/deserialization</li>
 *   <li>Test profile activation</li>
 * </ul>
 */
@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {ContainersConfig.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
