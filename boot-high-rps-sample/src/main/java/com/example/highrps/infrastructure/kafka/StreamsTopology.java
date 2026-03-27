package com.example.highrps.infrastructure.kafka;

import com.example.highrps.author.AuthorRequest;
import com.example.highrps.post.domain.requests.NewPostRequest;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableKafkaStreams
public class StreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(StreamsTopology.class);
    private final JsonMapper jsonMapper;

    public StreamsTopology(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Materialized KTable for posts: reads from posts-aggregates topic and
     * materializes to 'posts-store'
     * for interactive queries. Services can query this store to get the latest
     * state of posts.
     *
     * Uses byte[] deserialization to handle polymorphic events (PostCreatedEvent,
     * PostUpdatedEvent, PostDeletedEvent) and transforms them to NewPostRequest or null
     * for deletions.
     */
    @Bean
    public KTable<String, NewPostRequest> postsTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building posts KTable with materialized store");

        // Use ByteArray serde to handle polymorphic event types
        KTable<String, byte[]> rawTable = kafkaStreamBuilder.table(
                "posts-aggregates",
                Consumed.with(Serdes.String(), Serdes.ByteArray()));

        // Transform byte[] to NewPostRequest, handling deletion events
        KTable<String, NewPostRequest> table = rawTable.mapValues((key, bytes) -> {
            if (bytes == null) {
                // Tombstone (null value) -> return null for deletion
                log.debug("Received tombstone for key: {}", key);
                return null;
            }

            try {
                tools.jackson.databind.JsonNode node = jsonMapper.readTree(bytes);

                // Check for PostDeletedEvent (only has postId field)
                if (node.has("postId") && node.size() == 1) {
                    log.debug("Received PostDeletedEvent for key: {}, returning null", key);
                    return null;
                }

                // Deserialize to NewPostRequest for PostCreatedEvent/PostUpdatedEvent
                return jsonMapper.treeToValue(node, NewPostRequest.class);
            } catch (Exception e) {
                log.error("Failed to deserialize post aggregate for key: {}", key, e);
                // Return null to prevent downstream processing errors
                return null;
            }
        }, Materialized.as("posts-store"));

        log.info("Posts KTable materialized as 'posts-store'");
        return table;
    }

    /**
     * Materialized KTable for authors: reads from authors-aggregates topic and
     * materializes to 'authors-store'
     * for interactive queries. Services can query this store to get the latest
     * state of authors.
     */
    @Bean
    public KTable<String, AuthorRequest> authorsTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building authors KTable with materialized store");
        JacksonJsonSerde<AuthorRequest> authorSerde = new JacksonJsonSerde<>(AuthorRequest.class, jsonMapper);

        KTable<String, AuthorRequest> table = kafkaStreamBuilder.table(
                "authors-aggregates", Consumed.with(Serdes.String(), authorSerde), Materialized.as("authors-store"));

        log.info("Authors KTable materialized as 'authors-store'");
        return table;
    }

    /**
     * Materialized KTable for comments: reads from post-comments-aggregates topic
     * and materializes to 'post-comments-store'
     * for interactive queries. Services can query this store to get the latest
     * state of comments.
     */
    @Bean
    public KTable<String, PostCommentRequest> postCommentRequestKTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building comments KTable with materialized store");
        JacksonJsonSerde<PostCommentRequest> postCommentSerde =
                new JacksonJsonSerde<>(PostCommentRequest.class, jsonMapper);

        KTable<String, PostCommentRequest> table = kafkaStreamBuilder.table(
                "post-comments-aggregates",
                Consumed.with(Serdes.String(), postCommentSerde),
                Materialized.as("post-comments-store"));

        log.info("Comments KTable materialized as 'post-comments-store'");
        return table;
    }
}