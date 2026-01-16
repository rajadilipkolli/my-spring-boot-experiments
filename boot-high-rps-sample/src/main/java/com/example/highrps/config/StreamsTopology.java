package com.example.highrps.config;

import com.example.highrps.model.request.NewPostRequest;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
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
    public KTable<String, NewPostRequest> eventsStream(StreamsBuilder kafkaStreamBuilder) {
        log.info("Building events stream topology");
        JacksonJsonSerde<NewPostRequest> eventSerde = new JacksonJsonSerde<>(NewPostRequest.class);

        KStream<String, NewPostRequest> postRequestKStream =
                kafkaStreamBuilder.stream("events", Consumed.with(Serdes.String(), eventSerde));

        // Aggregate latest value per title into a persistent store named "posts-store"
        KTable<String, NewPostRequest> aggregates = postRequestKStream
                .groupByKey(Grouped.with(Serdes.String(), eventSerde))
                .reduce(
                        (oldV, newV) -> newV,
                        Materialized.<String, NewPostRequest>as(Stores.persistentKeyValueStore("posts-store"))
                                .withKeySerde(Serdes.String())
                                .withValueSerde(eventSerde));

        // Publish aggregates as simple string values so downstream consumers use String deserializers
        aggregates.toStream().to("posts-aggregates", Produced.with(Serdes.String(), eventSerde));

        return aggregates;
    }
}
