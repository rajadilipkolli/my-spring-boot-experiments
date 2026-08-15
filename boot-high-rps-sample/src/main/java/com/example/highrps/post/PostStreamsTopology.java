package com.example.highrps.post;

import com.example.highrps.infrastructure.kafka.KafkaSerdeFactory;
import com.example.highrps.post.domain.requests.NewPostRequest;
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

@Configuration(proxyBeanMethods = false)
public class PostStreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(PostStreamsTopology.class);

    private final KafkaSerdeFactory kafkaSerdeFactory;

    public PostStreamsTopology(KafkaSerdeFactory kafkaSerdeFactory) {
        this.kafkaSerdeFactory = kafkaSerdeFactory;
    }

    @Bean
    public KTable<String, NewPostRequest> postsTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building posts KTable with materialized store");
        Serde<NewPostRequest> postSerde = kafkaSerdeFactory.modulithCompatibleSerde(NewPostRequest.class);

        KTable<String, NewPostRequest> table = kafkaStreamBuilder.table(
                "posts-aggregates", Consumed.with(Serdes.String(), postSerde), Materialized.as("posts-store"));

        log.info("Posts KTable materialized as 'posts-store'");
        return table;
    }
}
