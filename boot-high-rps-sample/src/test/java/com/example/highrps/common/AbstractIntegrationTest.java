package com.example.highrps.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.highrps.repository.jpa.PostRepository;
import com.example.highrps.repository.jpa.PostTagRepository;
import com.example.highrps.repository.jpa.TagRepository;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ContainersConfig.class)
@AutoConfigureMockMvc
@AutoConfigureTracing
@AutoConfigureMetrics
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected Cache<String, String> localCache;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected PostTagRepository postTagRepository;

    @Autowired
    protected MeterRegistry meterRegistry;

    @Autowired
    protected AuthorRedisRepository authorRedisRepository;
}
