package com.example.learning.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.r2dbc.postgresql.codec.Json;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
class PgJsonObjectJsonComponent {

    static class Deserializer extends JsonDeserializer<Json> {

        private static final Logger log = LoggerFactory.getLogger(Deserializer.class);

        @Override
        public Json deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var value = ctxt.readTree(p);
            log.debug("read json value :{}", value);
            return Json.of(value.toString());
        }
    }

    static class Serializer extends JsonSerializer<Json> {

        private static final Logger log = LoggerFactory.getLogger(Serializer.class);

        @Override
        public void serialize(Json value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            var text = value.asString();
            log.debug("The raw json value from PostgresSQL JSON type:{}", text);
            JsonFactory factory = new JsonFactory();
            try (JsonParser parser = factory.createParser(text)) {
                var node = gen.getCodec().readTree(parser);
                serializers.defaultSerializeValue(node, gen);
            }
        }
    }
}
