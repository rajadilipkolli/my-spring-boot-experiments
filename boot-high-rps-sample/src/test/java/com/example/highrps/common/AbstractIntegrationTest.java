package com.example.highrps.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.highrps.HighRpsApplication;
import com.example.highrps.infrastructure.kafka.batch.AuthorBatchProcessor;
import com.example.highrps.repository.jpa.AuthorRepository;
import com.example.highrps.repository.jpa.PostCommentRepository;
import com.example.highrps.repository.jpa.PostRepository;
import com.example.highrps.repository.jpa.PostTagRepository;
import com.example.highrps.repository.jpa.TagRepository;
import com.example.highrps.repository.redis.AuthorRedisRepository;
import com.example.highrps.repository.redis.PostRedisRepository;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles({"test"})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {HighRpsApplication.class, ContainersConfig.class, SQLContainerConfig.class})
@AutoConfigureMockMvc
@AutoConfigureTracing
@AutoConfigureMetrics
public abstract class AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @Autowired
    protected MockMvcTester mockMvcTester;

    @Autowired
    protected Cache<String, String> localCache;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @Autowired
    protected AuthorRepository authorRepository;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected PostCommentRepository postCommentRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected PostTagRepository postTagRepository;

    @Autowired
    protected MeterRegistry meterRegistry;

    @Autowired
    protected AuthorRedisRepository authorRedisRepository;

    @Autowired
    protected PostRedisRepository postRedisRepository;

    @Autowired
    protected AuthorBatchProcessor authorBatchProcessor;

    @Autowired
    protected JsonMapper jsonMapper;

    @BeforeEach
    public void clearDatabase() {
        postCommentRepository.deleteAllInBatch();
        postTagRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        authorRepository.deleteAllInBatch();

        authorRedisRepository.deleteAll();
        postRedisRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        localCache.invalidateAll();
    }
}
