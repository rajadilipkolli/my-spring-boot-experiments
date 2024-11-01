package com.example.multipledatasources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class ApplicationIntegrationTest {

    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:9.1");

    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.0-alpine"));

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.cardholder.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("app.datasource.cardholder.username", MY_SQL_CONTAINER::getUsername);
        registry.add("app.datasource.cardholder.password", MY_SQL_CONTAINER::getPassword);
        registry.add("app.datasource.member.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("app.datasource.member.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("app.datasource.member.password", POSTGRE_SQL_CONTAINER::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void verifyBootStrap() throws Exception {

        this.mockMvc
                .perform(get("/details/{memberId}", "1").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        content()
                                .json(
                                        """
                {"memberId":"1","cardNumber":"1234-5678-9012-3456","memberName":"raja"}
                """));
    }
}
