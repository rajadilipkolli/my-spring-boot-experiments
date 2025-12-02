package com.example.custom.sequence.common;

import static com.example.custom.sequence.utils.AppConstants.PROFILE_TEST;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base class for integration tests providing test infrastructure including: -
 * Configured DataSource
 * using LazyConnectionDataSourceProxy - MockMvc for API testing - ObjectMapper
 * for JSON
 * serialization
 *
 * <p>
 * Uses ContainersConfig for test container configuration.
 */
@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainersConfig.class)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected DataSource dataSource;
}
