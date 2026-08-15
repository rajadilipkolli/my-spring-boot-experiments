package com.example.highrps.author;

import com.example.highrps.author.dto.AuthorRequest;
import com.example.highrps.infrastructure.kafka.KafkaSerdeFactory;
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
public class AuthorStreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(AuthorStreamsTopology.class);

    private final KafkaSerdeFactory kafkaSerdeFactory;

    public AuthorStreamsTopology(KafkaSerdeFactory kafkaSerdeFactory) {
        this.kafkaSerdeFactory = kafkaSerdeFactory;
    }

    @Bean
    public KTable<String, AuthorRequest> authorsTable(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building authors KTable with materialized store");
        Serde<AuthorRequest> authorSerde = kafkaSerdeFactory.modulithCompatibleSerde(AuthorRequest.class);

        KTable<String, AuthorRequest> table = kafkaStreamBuilder.table(
                "authors-aggregates", Consumed.with(Serdes.String(), authorSerde), Materialized.as("authors-store"));

        log.info("Authors KTable materialized as 'authors-store'");
        return table;
    }
}
