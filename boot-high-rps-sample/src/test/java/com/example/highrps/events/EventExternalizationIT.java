package com.example.highrps.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.HighRpsApplication;
import com.example.highrps.author.command.AuthorCommandService;
import com.example.highrps.author.command.CreateAuthorCommand;
import com.example.highrps.author.domain.events.AuthorCreatedEvent;
import com.example.highrps.common.ContainersConfig;
import com.example.highrps.common.SQLContainerConfig;
import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.command.PostCommandService;
import com.example.highrps.post.domain.events.PostCreatedEvent;
import com.example.highrps.postcomment.command.CreatePostCommentCommand;
import com.example.highrps.postcomment.command.PostCommentCommandService;
import com.example.highrps.postcomment.domain.events.PostCommentCreatedEvent;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Integration tests for Spring Modulith event externalization to Kafka.
 * Verifies that domain events annotated with @Externalized are properly
 * published to Kafka topics.
 *
 * <p>
 * Uses @EmbeddedKafka to simulate a real Kafka broker for testing.
 */
@SpringBootTest(classes = {HighRpsApplication.class, ContainersConfig.class, SQLContainerConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventExternalizationIT {

    @Autowired
    private PostCommandService postCommandService;

    @Autowired
    private AuthorCommandService authorCommandService;

    @Autowired
    private PostCommentCommandService postCommentCommandService;

    @Autowired
    private org.testcontainers.kafka.KafkaContainer kafkaContainer;

    private Map<String, Object> receivedEvents;

    @BeforeEach
    void setUp() {
        receivedEvents = new ConcurrentHashMap<>();
    }

    @Test
    @DisplayName("Should externalize PostCreatedEvent to Kafka topic 'posts-aggregates'")
    void shouldExternalizePostCreatedEventToKafka() {
        // Arrange - Set up Kafka consumer
        KafkaMessageListenerContainer<String, PostCreatedEvent> container =
                createKafkaConsumer("posts-aggregates", PostCreatedEvent.class);
        container.setupMessageListener((MessageListener<String, PostCreatedEvent>) record -> {
            receivedEvents.put("postCreated", record.value());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);

        // Act - Create a post (which should publish PostCreatedEvent)
        CreatePostCommand command = new CreatePostCommand(12345L, "Test Post", "Test Content", "author@test.com", true);
        postCommandService.createPost(command);

        // Assert - Verify event was externalized to Kafka
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(receivedEvents).containsKey("postCreated");
            PostCreatedEvent event = (PostCreatedEvent) receivedEvents.get("postCreated");
            assertThat(event.postId()).isEqualTo(12345L);
            assertThat(event.title()).isEqualTo("Test Post");
            assertThat(event.content()).isEqualTo("Test Content");
            assertThat(event.authorEmail()).isEqualTo("author@test.com");
            assertThat(event.published()).isTrue();
        });

        container.stop();
    }

    @Test
    @DisplayName("Should externalize AuthorCreatedEvent to Kafka topic 'authors-aggregates'")
    void shouldExternalizeAuthorCreatedEventToKafka() {
        // Arrange - Set up Kafka consumer
        KafkaMessageListenerContainer<String, AuthorCreatedEvent> container =
                createKafkaConsumer("authors-aggregates", AuthorCreatedEvent.class);
        container.setupMessageListener((MessageListener<String, AuthorCreatedEvent>) record -> {
            receivedEvents.put("authorCreated", record.value());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);

        // Act - Create an author (which should publish AuthorCreatedEvent)
        CreateAuthorCommand command = new CreateAuthorCommand("test@example.com", "Test", "Author", 1234567890L);
        authorCommandService.createAuthor(command);

        // Assert - Verify event was externalized to Kafka
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(receivedEvents).containsKey("authorCreated");
            AuthorCreatedEvent event = (AuthorCreatedEvent) receivedEvents.get("authorCreated");
            assertThat(event.email()).isEqualTo("test@example.com");
            assertThat(event.firstName()).isEqualTo("Test");
            assertThat(event.lastName()).isEqualTo("Author");
            assertThat(event.mobile()).isEqualTo(1234567890L);
        });

        container.stop();
    }

    @Test
    @DisplayName("Should externalize PostCommentCreatedEvent to Kafka topic 'postcomments'")
    void shouldExternalizePostCommentCreatedEventToKafka() {
        // Arrange - Set up Kafka consumer
        KafkaMessageListenerContainer<String, PostCommentCreatedEvent> container =
                createKafkaConsumer("postcomments", PostCommentCreatedEvent.class);
        container.setupMessageListener((MessageListener<String, PostCommentCreatedEvent>) record -> {
            receivedEvents.put("commentCreated", record.value());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, 1);

        // Act - Create a comment (which should publish PostCommentCreatedEvent)
        CreatePostCommentCommand command =
                new CreatePostCommentCommand("Great post!", "commenter@test.com", 12345L, true);
        postCommentCommandService.createComment(command);

        // Assert - Verify event was externalized to Kafka
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(receivedEvents).containsKey("commentCreated");
            PostCommentCreatedEvent event = (PostCommentCreatedEvent) receivedEvents.get("commentCreated");
            assertThat(event.id()).isEqualTo(999L);
            assertThat(event.postId()).isEqualTo(12345L);
            assertThat(event.reviewText()).isEqualTo("Great post!");
        });

        container.stop();
    }

    /**
     * Helper method to create a Kafka consumer for testing.
     */
    private <T> KafkaMessageListenerContainer<String, T> createKafkaConsumer(String topic, Class<T> valueType) {
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-group-" + topic,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES,
                "*",
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                valueType.getName());

        DefaultKafkaConsumerFactory<String, T> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProps = new ContainerProperties(topic);

        return new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
    }
}
