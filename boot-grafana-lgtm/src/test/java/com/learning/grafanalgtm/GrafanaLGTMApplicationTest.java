package com.learning.grafanalgtm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = {TestGrafanaLGTMApplication.class})
@AutoConfigureMockMvc
class GrafanaLGTMApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void prometheus(){

    }
}