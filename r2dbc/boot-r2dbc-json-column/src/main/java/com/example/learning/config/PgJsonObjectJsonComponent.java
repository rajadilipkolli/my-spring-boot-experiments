package com.example.learning.config;

import io.r2dbc.postgresql.codec.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

@JacksonComponent
class PgJsonObjectJsonComponent {

    static class Deserializer extends ValueDeserializer<Json> {

        private static final Logger log = LoggerFactory.getLogger(Deserializer.class);

        @Override
        public Json deserialize(JsonParser p, DeserializationContext ctxt) {
            var value = ctxt.readTree(p);
            log.debug("read json value :{}", value);
            return Json.of(value.toString());
        }
    }

    static class Serializer extends ValueSerializer<Json> {

        private static final Logger log = LoggerFactory.getLogger(Serializer.class);

        @Override
        public void serialize(Json value, JsonGenerator gen, SerializationContext ctxt) {
            var text = value.asString();
            log.debug("The raw json value from PostgresSQL JSON type:{}", text);
            // write the JSON string as raw JSON content so it remains structured
            gen.writeRawValue(text);
        }
    }
}
