package com.learning.grafanalgtm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest(classes = {TestGrafanaLGTMApplication.class})
@AutoConfigureMockMvc
class GrafanaLGTMApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenericContainer<?> lgtmContainer;

    @Test
    void prometheus(){
        System.out.println("Configured grafana port "+ lgtmContainer.getMappedPort(3000));
    }
}