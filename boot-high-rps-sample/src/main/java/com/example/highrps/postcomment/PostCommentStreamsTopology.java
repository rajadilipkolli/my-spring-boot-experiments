package com.example.highrps.postcomment;

import com.example.highrps.infrastructure.kafka.KafkaSerdeFactory;
import com.example.highrps.postcomment.domain.PostCommentRequest;
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

        KTable<String, PostCommentRequest> table = kafkaStreamBuilder.table(
                "post-comments-aggregates",
                Consumed.with(Serdes.String(), postCommentSerde),
                Materialized.as("post-comments-store"));

        log.info("Comments KTable materialized as 'post-comments-store'");
        return table;
    }
}
