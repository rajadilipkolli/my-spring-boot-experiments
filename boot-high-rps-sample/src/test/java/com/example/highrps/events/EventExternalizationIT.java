package com.example.highrps.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.highrps.author.command.CreateAuthorCommand;
import com.example.highrps.common.AbstractIntegrationTest;
import com.example.highrps.post.command.CreatePostCommand;
import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import com.example.highrps.postcomment.command.CreatePostCommentCommand;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;

/**
 * Integration tests for Spring Modulith event externalization to Kafka.
 * Verifies that domain events annotated with @Externalized are properly
 * published to Kafka topics.
 *
 * <p>
 * Spring Modulith serializes externalized events as JSON byte arrays,
 * so we use StringDeserializer and parse the JSON manually.
 */
class EventExternalizationIT extends AbstractIntegrationTest {

    private Map<String, String> receivedEvents;

    @BeforeEach
    void setUp() throws Exception {
        receivedEvents = new ConcurrentHashMap<>();
        // List all topics to verify auto-creation
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionDetails.getBootstrapServers());
        try (AdminClient adminClient = AdminClient.create(props)) {
            var topics = adminClient.listTopics().names().get();
            System.out.println("Available Kafka Topics: " + topics);
        }
    }

    @Test
    @DisplayName("Should externalize PostCreatedEvent to Kafka topic 'posts-aggregates'")
    void shouldExternalizePostCreatedEventToKafka() {
        // Arrange - Set up Kafka consumer
        String topic = "posts-aggregates";
        int partitions = getPartitionCount(topic);
        KafkaMessageListenerContainer<String, String> container = createKafkaConsumer(topic);
        container.setupMessageListener((MessageListener<String, String>) record -> {
            receivedEvents.put("postCreated", record.value());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, partitions);

        // Act - Create a post (which should publish PostCreatedEvent)
        String email = "author-" + UUID.randomUUID() + "@test.com";
        CreatePostCommand createCmd = new CreatePostCommand(
                12345L,
                "Test Post",
                "Test Content",
                email,
                true,
                new PostDetailsRequest("initial-key", "tester"),
                List.of(new TagRequest("tag1", "desc1")));
        postCommandService.createPost(createCmd);

        // Assert - Verify event was externalized to Kafka
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(receivedEvents).containsKey("postCreated");
            var rawJson = receivedEvents.get("postCreated");
            if (rawJson != null && rawJson.startsWith("\"eyJ")) {
                String inner = jsonMapper.readTree(rawJson).asString();
                rawJson = new String(java.util.Base64.getDecoder().decode(inner));
            }
            assertThat(rawJson).isNotNull();
            System.out.println("Received PostCreatedEvent JSON: " + rawJson);
            var json = jsonMapper.readTree(rawJson);
            assertThat(json.get("postId").asLong()).isEqualTo(12345L);
            assertThat(json.get("title").asString()).isEqualTo("Test Post");
            assertThat(json.get("content").asString()).isEqualTo("Test Content");
            assertThat(json.get("authorEmail").asString()).isEqualTo(email);
            assertThat(json.get("published").asBoolean()).isTrue();
        });

        container.stop();
    }

    @Test
    @DisplayName("Should externalize AuthorCreatedEvent to Kafka topic 'authors-aggregates'")
    void shouldExternalizeAuthorCreatedEventToKafka() {
        // Arrange - Set up Kafka consumer
        String topic = "authors-aggregates";
        int partitions = getPartitionCount(topic);
        KafkaMessageListenerContainer<String, String> container = createKafkaConsumer(topic);
        container.setupMessageListener((MessageListener<String, String>) record -> {
            receivedEvents.put("authorCreated", record.value());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, partitions);

        // Act - Create an author (which should publish AuthorCreatedEvent)
        String email = "test-ext-" + UUID.randomUUID() + "@example.com";
        CreateAuthorCommand command =
                new CreateAuthorCommand(email, "Test", null, "Author", 1234567890L, LocalDateTime.now());
        authorCommandService.createAuthor(command);

        // Assert - Verify event was externalized to Kafka
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(receivedEvents).containsKey("authorCreated");
            var rawJson = receivedEvents.get("authorCreated");
            if (rawJson != null && rawJson.startsWith("\"eyJ")) {
                String inner = jsonMapper.readTree(rawJson).asString();
                rawJson = new String(java.util.Base64.getDecoder().decode(inner));
            }
            assertThat(rawJson).isNotNull();
            System.out.println("Received AuthorCreatedEvent JSON: " + rawJson);
            var json = jsonMapper.readTree(rawJson);
            assertThat(json.get("email").asString()).isEqualTo(email);
            assertThat(json.get("firstName").asString()).isEqualTo("Test");
            assertThat(json.get("lastName").asString()).isEqualTo("Author");
            assertThat(json.get("mobile").asLong()).isEqualTo(1234567890L);
        });

        container.stop();
    }

    @Test
    @DisplayName("Should externalize PostCommentCreatedEvent to Kafka topic 'post-comments-aggregates'")
    void shouldExternalizePostCommentCreatedEventToKafka() {
        String authorEmail = "comment-ext-author-" + UUID.randomUUID() + "@test.com";
        authorCommandService.createAuthor(
                new CreateAuthorCommand(authorEmail, "Comment", null, "Author", 9876543210L, LocalDateTime.now()));
        postCommandService.createPost(new CreatePostCommand(
                54321L,
                "Post for Comment",
                "Content",
                authorEmail,
                true,
                new PostDetailsRequest("comment-key", "author"),
                List.of(new TagRequest("comments", "desc"))));

        // Arrange - Set up Kafka consumer
        String topic = "post-comments-aggregates";
        int partitions = getPartitionCount(topic);
        KafkaMessageListenerContainer<String, String> container = createKafkaConsumer(topic);
        container.setupMessageListener((MessageListener<String, String>) record -> {
            receivedEvents.put("commentCreated", record.value());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, partitions);

        // Act - Create a comment (which should publish PostCommentCreatedEvent)
        CreatePostCommentCommand command =
                new CreatePostCommentCommand("Great post!", "Excellent content", 54321L, true);
        postCommentCommandService.createComment(command);

        // Assert - Verify event was externalized to Kafka
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(receivedEvents).containsKey("commentCreated");
            var rawJson = receivedEvents.get("commentCreated");
            if (rawJson != null && rawJson.startsWith("\"eyJ")) {
                String inner = jsonMapper.readTree(rawJson).asString();
                rawJson = new String(java.util.Base64.getDecoder().decode(inner));
            }
            assertThat(rawJson).isNotNull();
            System.out.println("Received PostCommentCreatedEvent JSON: " + rawJson);
            var json = jsonMapper.readTree(rawJson);
            assertThat(json.get("commentId").asLong()).isPositive();
            assertThat(json.get("postId").asLong()).isEqualTo(54321L);
            assertThat(json.get("content").asString()).isEqualTo("Excellent content");
        });

        container.stop();
    }

    /**
     * Helper method to create a Kafka consumer for testing.
     * Uses StringDeserializer because Spring Modulith serializes externalized
     * events as raw JSON bytes, not typed Jackson objects.
     */
    private KafkaMessageListenerContainer<String, String> createKafkaConsumer(String topic) {
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
                StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProps = new ContainerProperties(topic);

        return new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
    }

    /**
     * Get the actual partition count for a topic from the broker.
     */
    private int getPartitionCount(String topic) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        try (AdminClient adminClient = AdminClient.create(props)) {
            var topicDescription = adminClient
                    .describeTopics(Collections.singletonList(topic))
                    .allTopicNames()
                    .get();
            if (topicDescription.containsKey(topic)) {
                return topicDescription.get(topic).partitions().size();
            }
            return 1;
        } catch (Exception e) {
            System.out.println("Could not get partition count for topic: " + topic + ", defaulting to 1. Error: "
                    + e.getMessage());
            return 1;
        }
    }
}
