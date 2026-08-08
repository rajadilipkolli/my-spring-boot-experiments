package com.example.ultimateredis.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.ultimateredis.repository.ActorRepository;
import com.example.ultimateredis.service.RedisConsumer;
import com.example.ultimateredis.utils.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles({AppConstants.PROFILE_STANDALONE})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {TestcontainersConfiguration.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected ActorRepository actorRepository;

    @Autowired
    protected RedisConsumer redisConsumer;

    @Autowired
    protected MeterRegistry meterRegistry;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;
}
