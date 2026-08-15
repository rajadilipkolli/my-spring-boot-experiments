package com.example.highrps.common;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.highrps.HighRpsApplication;
import com.example.highrps.author.batch.AuthorBatchProcessor;
import com.example.highrps.author.command.AuthorCommandService;
import com.example.highrps.author.domain.AuthorRedisRepository;
import com.example.highrps.author.domain.AuthorRepository;
import com.example.highrps.infrastructure.kafka.batch.ScheduledBatchProcessor;
import com.example.highrps.post.command.PostCommandService;
import com.example.highrps.post.domain.PostRedisRepository;
import com.example.highrps.post.domain.PostRepository;
import com.example.highrps.post.domain.PostTagRepository;
import com.example.highrps.post.domain.TagRepository;
import com.example.highrps.postcomment.command.PostCommentCommandService;
import com.example.highrps.postcomment.domain.PostCommentRedisRepository;
import com.example.highrps.postcomment.domain.PostCommentRepository;
import com.example.highrps.shared.config.AppProperties;
import com.github.benmanes.caffeine.cache.Cache;
import eu.rekawek.toxiproxy.Proxy;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.streams.KafkaStreams;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {HighRpsApplication.class, ContainersConfig.class, SQLContainerConfig.class})
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "spring.kafka.streams.cleanup.on-startup=true",
            "spring.kafka.streams.cleanup.on-shutdown=true",
            "app.batch.delay-ms=99999999"
        })
@AutoConfigureMockMvc
@AutoConfigureTracing
@AutoConfigureMetrics
public abstract class AbstractIntegrationTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractIntegrationTest.class);
    private static final int KAFKA_STREAMS_TIMEOUT_SECONDS = 120;

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
    protected Proxy kafkaProxy;

    @Autowired
    protected KafkaConnectionDetails kafkaConnectionDetails;

    @Autowired
    protected StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected List<ScheduledBatchProcessor> scheduledBatchProcessors;

    @Autowired
    protected AppProperties appProperties;

    @Autowired
    protected ProducerFactory<String, Object> producerFactory;

    @Autowired
    protected ApplicationContext applicationContext;

    public void clearDatabase() {
        clearDatabase(false);
    }

    public void clearDatabase(boolean faultInjectionOptIn) {
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

        // Re-initialize consumer groups after flushDb
        if (scheduledBatchProcessors != null) {
            scheduledBatchProcessors.forEach(ScheduledBatchProcessor::init);
        }

        // Wait for Kafka Streams to be ready before proceeding with tests
        try {
            await().atMost(Duration.ofSeconds(KAFKA_STREAMS_TIMEOUT_SECONDS))
                    .pollInterval(Duration.ofMillis(500))
                    .until(() -> {
                        KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                        return streams != null && streams.state() == KafkaStreams.State.RUNNING;
                    });
        } catch (ConditionTimeoutException e) {
            if (faultInjectionOptIn) {
                log.warn(
                        "Kafka Streams did not become RUNNING within {} seconds. Proceeding anyway. State may have been affected by Toxiproxy.",
                        KAFKA_STREAMS_TIMEOUT_SECONDS,
                        e);
            } else {
                log.error("Kafka Streams did not become RUNNING within {} seconds.", KAFKA_STREAMS_TIMEOUT_SECONDS, e);
                throw e;
            }
        }
    }
}
