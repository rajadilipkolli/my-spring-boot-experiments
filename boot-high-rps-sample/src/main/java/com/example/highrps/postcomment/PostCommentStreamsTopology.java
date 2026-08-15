package com.example.highrps.postcomment;

import com.example.highrps.infrastructure.kafka.KafkaSerdeFactory;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;

@Configuration(proxyBeanMethods = false)
public class PostCommentStreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(PostCommentStreamsTopology.class);

    private final KafkaSerdeFactory kafkaSerdeFactory;

    public PostCommentStreamsTopology(KafkaSerdeFactory kafkaSerdeFactory) {
        this.kafkaSerdeFactory = kafkaSerdeFactory;
    }

    @Bean
    public KTable<String, PostCommentRequest> postCommentRequestKTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building comments KTable with materialized store");
        Serde<PostCommentRequest> postCommentSerde =
                kafkaSerdeFactory.modulithCompatibleSerde(PostCommentRequest.class);
        Serde<JsonNode> jsonNodeSerde = kafkaSerdeFactory.modulithCompatibleSerde(JsonNode.class);

        KTable<String, PostCommentRequest> table = kafkaStreamBuilder.stream(
                        "post-comments-aggregates", Consumed.with(Serdes.String(), jsonNodeSerde))
                .mapValues(node -> {
                    if (node == null || node.isNull()) return null;

                    // Deleted event mapping
                    if (!node.has("title") || node.get("title").isNull()) {
                        return null;
                    }

                    // Created or updated event mapping
                    Long commentId =
                            node.has("commentId") ? node.get("commentId").asLong() : null;
                    Long postId = node.has("postId") ? node.get("postId").asLong() : null;
                    String title = node.has("title") ? node.get("title").asString() : null;
                    String content = node.has("content") ? node.get("content").asString() : null;
                    Boolean published =
                            node.has("published") ? node.get("published").asBoolean() : null;

                    OffsetDateTime publishedAt = null;
                    if (node.has("publishedAt") && !node.get("publishedAt").isNull()) {
                        publishedAt =
                                OffsetDateTime.parse(node.get("publishedAt").asString());
                    }

                    LocalDateTime createdAt = null;
                    if (node.has("createdAt") && !node.get("createdAt").isNull()) {
                        String createdAtStr = node.get("createdAt").asString();
                        try {
                            createdAt = OffsetDateTime.parse(createdAtStr).toLocalDateTime();
                        } catch (Exception e) {
                            try {
                                createdAt = LocalDateTime.parse(createdAtStr);
                            } catch (Exception ex) {
                                // ignore
                            }
                        }
                    }

                    LocalDateTime modifiedAt = null;
                    if (node.has("modifiedAt") && !node.get("modifiedAt").isNull()) {
                        modifiedAt = LocalDateTime.parse(node.get("modifiedAt").asString());
                    }

                    return new PostCommentRequest(
                            commentId, postId, title, content, published, publishedAt, createdAt, modifiedAt);
                })
                .toTable(Materialized
                        .<String, PostCommentRequest,
                                org.apache.kafka.streams.state.KeyValueStore<
                                        org.apache.kafka.common.utils.Bytes, byte[]>>
                                as("post-comments-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(postCommentSerde));

        log.info("Comments KTable materialized as 'post-comments-store'");
        return table;
    }
}
