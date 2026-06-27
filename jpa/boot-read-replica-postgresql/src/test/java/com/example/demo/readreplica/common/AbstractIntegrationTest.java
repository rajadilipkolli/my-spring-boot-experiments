package com.example.demo.readreplica.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.demo.readreplica.ReadReplicaApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles({"test"})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {ContainersConfig.class, ReadReplicaApplication.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvcTester mockMvcTester;

    @Autowired protected JsonMapper jsonMapper;
}
