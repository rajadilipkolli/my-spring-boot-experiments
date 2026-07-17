package com.example.highrps.common;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.highrps.HighRpsApplication;
import com.example.highrps.author.batch.AuthorBatchProcessor;
import com.example.highrps.author.command.AuthorCommandService;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.domain.AuthorRepository;
import com.example.highrps.post.command.PostCommandService;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.PostRepository;
import com.example.highrps.post.domain.PostTagRepository;
import com.example.highrps.post.domain.TagRepository;
import com.example.highrps.postcomment.command.PostCommentCommandService;
import com.example.highrps.postcomment.domain.PostCommentRedisRepository;
import com.example.highrps.postcomment.domain.PostCommentRepository;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.json.JsonMapper;

@ActiveProfiles({"test"})
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {HighRpsApplication.class, ContainersConfig.class, SQLContainerConfig.class})
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
    protected PostCommentRedisRepository postCommentRedisRepository;

    @Autowired
    protected AuthorBatchProcessor authorBatchProcessor;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected PostCommandService postCommandService;

    @Autowired
    protected AuthorCommandService authorCommandService;

    @Autowired
    protected PostCommentCommandService postCommentCommandService;

    @Autowired
    protected KafkaContainer kafkaContainer;

    @Autowired
    protected KafkaConnectionDetails kafkaConnectionDetails;

    @Autowired
    protected StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeEach
    public void clearDatabase() {
        postCommentRepository.deleteAllInBatch();
        postTagRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
        authorRepository.deleteAllInBatch();

        authorRedisRepository.deleteAll();
        postRedisRepository.deleteAll();
        postCommentRedisRepository.deleteAll();
        redisTemplate.execute(
                connection -> {
                    connection.serverCommands().flushDb();
                    return null;
                },
                true);
        localCache.invalidateAll();

        // Wait for Kafka Streams to be ready before proceeding with tests
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    return streams != null && streams.state() == KafkaStreams.State.RUNNING;
                });
    }
}
