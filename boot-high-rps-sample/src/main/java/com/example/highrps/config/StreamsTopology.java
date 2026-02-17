package com.example.highrps.config;

import com.example.highrps.model.request.AuthorRequest;
import com.example.highrps.model.request.EventEnvelope;
import com.example.highrps.model.request.NewPostRequest;
import com.example.highrps.postcomment.domain.PostCommentRequest;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
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

    private final JsonMapper mapper;

    public StreamsTopology(JsonMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Generic envelope router: reads from 'events' topic and routes to per-entity aggregate topics
     * based on the 'entity' field in EventEnvelope. This allows adding new entities without changing
     * this topology - just add a new filter + route.
     */
    @Bean
    public KStream<String, EventEnvelope> eventsStream(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building events stream topology (generic envelope)");

        JacksonJsonSerde<EventEnvelope> envelopeSerde = new JacksonJsonSerde<>(EventEnvelope.class);
        JacksonJsonSerde<NewPostRequest> postSerde = new JacksonJsonSerde<>(NewPostRequest.class);
        JacksonJsonSerde<AuthorRequest> authorSerde = new JacksonJsonSerde<>(AuthorRequest.class);
        JacksonJsonSerde<PostCommentRequest> postCommentSerde = new JacksonJsonSerde<>(PostCommentRequest.class);

        // Read the generic events topic as KStream of EventEnvelope
        KStream<String, EventEnvelope> envelopeStream =
                kafkaStreamBuilder.stream("events", Consumed.with(Serdes.String(), envelopeSerde));

        // posts stream: filter envelopes with entity == 'post' and convert payload to NewPostRequest
        KStream<String, NewPostRequest> posts = envelopeStream
                .filter((k, env) -> env != null && "post".equalsIgnoreCase(env.entity()))
                .mapValues(env -> mapper.convertValue(env.payload(), NewPostRequest.class));

        posts.to("posts-aggregates", Produced.with(Serdes.String(), postSerde));

        // authors stream: filter envelopes with entity == 'author' and convert payload to AuthorRequest
        KStream<String, AuthorRequest> authors = envelopeStream
                .filter((k, env) -> env != null && "author".equalsIgnoreCase(env.entity()))
                .mapValues(env -> mapper.convertValue(env.payload(), AuthorRequest.class));

        authors.to("authors-aggregates", Produced.with(Serdes.String(), authorSerde));

        // comments stream: filter envelopes with entity == 'post-comment' and convert payload to PostCommentRequest
        KStream<String, PostCommentRequest> comments = envelopeStream
                .filter((k, env) -> env != null && "post-comment".equalsIgnoreCase(env.entity()))
                .mapValues(env -> mapper.convertValue(env.payload(), PostCommentRequest.class));

        comments.to("post-comments-aggregates", Produced.with(Serdes.String(), postCommentSerde));

        log.info(
                "Streams topology for events -> posts-aggregates/authors-aggregates/post-comments-aggregates registered");

        return envelopeStream; // Return the stream to satisfy Spring's @Bean contract
    }

    /**
     * Materialized KTable for posts: reads from posts-aggregates topic and materializes to 'posts-store'
     * for interactive queries. Services can query this store to get the latest state of posts.
     */
    @Bean
    public KTable<String, NewPostRequest> postsTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building posts KTable with materialized store");
        JacksonJsonSerde<NewPostRequest> postSerde = new JacksonJsonSerde<>(NewPostRequest.class);

        KTable<String, NewPostRequest> table = kafkaStreamBuilder.table(
                "posts-aggregates", Consumed.with(Serdes.String(), postSerde), Materialized.as("posts-store"));

        log.info("Posts KTable materialized as 'posts-store'");
        return table;
    }

    /**
     * Materialized KTable for authors: reads from authors-aggregates topic and materializes to 'authors-store'
     * for interactive queries. Services can query this store to get the latest state of authors.
     */
    @Bean
    public KTable<String, AuthorRequest> authorsTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building authors KTable with materialized store");
        JacksonJsonSerde<AuthorRequest> authorSerde = new JacksonJsonSerde<>(AuthorRequest.class);

        KTable<String, AuthorRequest> table = kafkaStreamBuilder.table(
                "authors-aggregates", Consumed.with(Serdes.String(), authorSerde), Materialized.as("authors-store"));

        log.info("Authors KTable materialized as 'authors-store'");
        return table;
    }

    /**
     * Materialized KTable for comments: reads from post-comments-aggregates topic and materializes to 'post-comments-store'
     * for interactive queries. Services can query this store to get the latest state of comments.
     */
    @Bean
    public KTable<String, PostCommentRequest> postCommentRequestKTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building comments KTable with materialized store");
        JacksonJsonSerde<PostCommentRequest> postCommentSerde = new JacksonJsonSerde<>(PostCommentRequest.class);

        KTable<String, PostCommentRequest> table = kafkaStreamBuilder.table(
                "post-comments-aggregates",
                Consumed.with(Serdes.String(), postCommentSerde),
                Materialized.as("post-comments-store"));

        log.info("Comments KTable materialized as 'post-comments-store'");
        return table;
    }
}
