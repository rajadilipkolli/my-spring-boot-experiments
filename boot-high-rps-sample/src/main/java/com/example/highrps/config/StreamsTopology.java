package com.example.highrps.config;

import com.example.highrps.model.EventDto;
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
    public KTable<String, Long> eventsStream(StreamsBuilder builder) {
        log.info("Building events stream topology");
        JacksonJsonSerde<EventDto> eventSerde = new JacksonJsonSerde<>(EventDto.class);

        KStream<String, EventDto> stream = builder.stream("events", Consumed.with(Serdes.String(), eventSerde));

        // Aggregate latest value per id into a persistent store named "stats-store"
        KTable<String, Long> aggregates = stream.mapValues(EventDto::getValue)
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .reduce(
                        (oldV, newV) -> newV,
                        Materialized.<String, Long>as(Stores.persistentKeyValueStore("stats-store"))
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()));

        // Publish aggregates as simple string values so downstream consumers use String deserializers
        aggregates
                .toStream()
                .mapValues(Object::toString)
                .to("stats-aggregates", Produced.with(Serdes.String(), Serdes.String()));

        return aggregates;
    }
}
