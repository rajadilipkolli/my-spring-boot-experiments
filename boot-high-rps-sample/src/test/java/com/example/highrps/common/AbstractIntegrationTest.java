package com.example.highrps.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.highrps.HighRpsApplication;
import com.example.highrps.author.batch.AuthorBatchProcessor;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.domain.AuthorRepository;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.PostRepository;
import com.example.highrps.post.domain.PostTagRepository;
import com.example.highrps.post.domain.TagRepository;
import com.example.highrps.postcomment.domain.PostCommentRepository;
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
        redisTemplate.execute(
                connection -> {
                    connection.serverCommands().flushDb();
                    return null;
                },
                true);
        localCache.invalidateAll();
    }
}
