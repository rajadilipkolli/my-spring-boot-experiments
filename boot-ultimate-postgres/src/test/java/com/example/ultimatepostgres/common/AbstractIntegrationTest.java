package com.example.ultimatepostgres.common;

import com.example.ultimatepostgres.repository.JobQueueRepository;
import com.example.ultimatepostgres.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ContainersConfig.class)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected CacheService cacheService;

    @Autowired
    protected JobQueueRepository jobQueueRepository;

    @Autowired
    protected CacheCleanupTask cacheCleanupTask;

    @Autowired
    protected JobProducerService jobProducerService;

    @Autowired
    protected JobConsumerService jobConsumerService;

    @Autowired
    protected PubSubPublisher pubSubPublisher;

    @Autowired
    protected PubSubListener pubSubListener;

    @Autowired
    protected ObjectMapper objectMapper;
}
