package com.example.highrps.config;

import com.example.highrps.model.request.NewPostRequest;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
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

    @Bean
    KTable<String, NewPostRequest> eventsStream(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building events stream topology");
        JacksonJsonSerde<NewPostRequest> eventSerde = new JacksonJsonSerde<>(NewPostRequest.class);
        // Treat the events topic as a KTable and materialize it into the persistent store "posts-store".
        // This ensures tombstones (null values) are applied as deletes on the materialized store.
        KTable<String, NewPostRequest> eventsTable = kafkaStreamBuilder.table(
                "events",
                Consumed.with(Serdes.String(), eventSerde),
                Materialized.<String, NewPostRequest, KeyValueStore<Bytes, byte[]>>as("posts-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(eventSerde));

        // Publish aggregates downstream for further processing
        eventsTable.toStream().to("posts-aggregates", Produced.with(Serdes.String(), eventSerde));

        return eventsTable;
    }
}
