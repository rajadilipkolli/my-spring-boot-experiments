package com.example.ultimateredis.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.ultimateredis.repository.ActorRepository;
import com.example.ultimateredis.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles({AppConstants.PROFILE_STANDALONE})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {TestcontainersConfiguration.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ActorRepository actorRepository;
}
