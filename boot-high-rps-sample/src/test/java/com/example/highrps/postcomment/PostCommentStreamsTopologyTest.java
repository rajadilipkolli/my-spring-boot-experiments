package com.example.highrps.postcomment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.highrps.infrastructure.kafka.KafkaSerdeFactory;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import com.example.highrps.postcomment.domain.events.PostCommentCreatedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentDeletedEvent;
import com.example.highrps.postcomment.domain.events.PostCommentUpdatedEvent;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class PostCommentStreamsTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private KeyValueStore<String, PostCommentRequest> store;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().findAndAddModules().build();
        KafkaSerdeFactory serdeFactory = new KafkaSerdeFactory(jsonMapper);

        StreamsBuilder builder = new StreamsBuilder();
        PostCommentStreamsTopology topology = new PostCommentStreamsTopology(serdeFactory);
        topology.postCommentRequestKTable(builder);

        Properties props = new Properties();
        props.put(org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        testDriver = new TopologyTestDriver(builder.build(), props);
        inputTopic = testDriver.createInputTopic(
                "post-comments-aggregates",
                new Serdes.StringSerde().serializer(),
                new Serdes.StringSerde().serializer());
        store = testDriver.getKeyValueStore("post-comments-store");
    }

    @AfterEach
    void tearDown() {
        if (testDriver != null) {
            testDriver.close();
        }
    }

    @Test
    void testCreateUpdateDeleteAndReplay() throws Exception {
        OffsetDateTime createdOffset = OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        LocalDateTime createdLocal = createdOffset.toLocalDateTime();
        OffsetDateTime publishedOffset = OffsetDateTime.of(2023, 1, 1, 10, 5, 0, 0, ZoneOffset.UTC);

        // 1. Create Event
        PostCommentCreatedEvent createdEvent =
                new PostCommentCreatedEvent(1L, 100L, "Title", "Content", true, publishedOffset, createdOffset);

        inputTopic.pipeInput("1", jsonMapper.writeValueAsString(createdEvent));

        PostCommentRequest state = store.get("1");
        assertThat(state).isNotNull();
        assertThat(state.commentId()).isEqualTo(1L);
        assertThat(state.postId()).isEqualTo(100L);
        assertThat(state.title()).isEqualTo("Title");
        assertThat(state.content()).isEqualTo("Content");
        assertThat(state.published()).isTrue();
        assertThat(state.createdAt()).isEqualTo(createdLocal);
        assertThat(state.publishedAt()).isEqualTo(publishedOffset);

        // 2. Update Event
        LocalDateTime modifiedLocal = LocalDateTime.of(2023, 1, 2, 10, 0, 0, 0);
        PostCommentUpdatedEvent updatedEvent = new PostCommentUpdatedEvent(
                1L, 100L, "Title Updated", "Content Updated", false, null, createdLocal, modifiedLocal);

        inputTopic.pipeInput("1", jsonMapper.writeValueAsString(updatedEvent));

        state = store.get("1");
        assertThat(state).isNotNull();
        assertThat(state.title()).isEqualTo("Title Updated");
        assertThat(state.content()).isEqualTo("Content Updated");
        assertThat(state.published()).isFalse();
        assertThat(state.publishedAt()).isNull();
        assertThat(state.createdAt()).isEqualTo(createdLocal);
        assertThat(state.modifiedAt()).isEqualTo(modifiedLocal);

        // 3. Delete Event
        PostCommentDeletedEvent deletedEvent = new PostCommentDeletedEvent(1L, 100L);
        inputTopic.pipeInput("1", jsonMapper.writeValueAsString(deletedEvent));

        state = store.get("1");
        assertThat(state).isNull(); // Tombstone removes it from KTable

        // 4. Replay (Send Create again)
        inputTopic.pipeInput("1", jsonMapper.writeValueAsString(createdEvent));
        state = store.get("1");
        assertThat(state).isNotNull();
        assertThat(state.title()).isEqualTo("Title");
    }
}
