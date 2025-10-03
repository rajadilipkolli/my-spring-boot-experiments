package com.example.plugin.strategyplugin.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ActiveProfiles({"test"})
@SpringBootTest(
        webEnvironment = RANDOM_PORT
        //        classes = {ContainerConfig.class}
        )
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @LocalServerPort protected int port;

    @Autowired protected MockMvcTester mockMvcTester;
    @Autowired protected ObjectMapper objectMapper;
}
