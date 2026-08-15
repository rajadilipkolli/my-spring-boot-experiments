package com.example.highrps.infrastructure.kafka;

import java.util.Base64;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@NamedInterface("kafka")
@Component
public class KafkaSerdeFactory {

    private final JsonMapper jsonMapper;

    public KafkaSerdeFactory(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public <T> Serde<T> modulithCompatibleSerde(Class<T> type) {
        JacksonJsonSerde<T> baseSerde = new JacksonJsonSerde<>(type, jsonMapper);
        Serializer<T> serializer = baseSerde.serializer();

        Deserializer<T> modulithDeserializer = (topic, data) -> {
            if (data == null || data.length == 0) {
                return null;
            }
            // Try parsing as normal JSON first
            JsonNode node = jsonMapper.readTree(data);
            // Check if it is a Base64-encoded string (characteristic of some Spring Modulith setups)
            if (node.isString() && node.asString().startsWith("eyJ")) {
                byte[] decoded = Base64.getDecoder().decode(node.asString());
                return jsonMapper.readValue(decoded, type);
            }
            // Otherwise parse the node into the target type
            return jsonMapper.treeToValue(node, type);
        };

        return Serdes.serdeFrom(serializer, modulithDeserializer);
    }
}
