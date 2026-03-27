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

@Configuration
@EnableKafkaStreams
public class StreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(StreamsTopology.class);

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
