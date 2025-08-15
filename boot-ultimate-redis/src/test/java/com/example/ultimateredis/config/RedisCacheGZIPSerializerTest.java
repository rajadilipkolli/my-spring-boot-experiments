package com.example.ultimateredis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RedisCacheGZIPSerializerTest {

    private RedisCacheGZIPSerializer serializer;
    private static final byte[] GZIP_MARKER = "GZIP".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        serializer = new RedisCacheGZIPSerializer();
    }

    @Test
    void serialize_nullObject_shouldReturnEmptyByteArray() {
        // Act
        byte[] result = serializer.serialize(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void deserialize_nullBytes_shouldReturnNull() {
        // Act
        Object result = serializer.deserialize(null);

        // Assert
        assertNull(result);
    }

    @Test
    void deserialize_emptyBytes_shouldReturnNull() {
        // Act
        Object result = serializer.deserialize(new byte[0]);

        // Assert
        assertNull(result);
    }

    @Test
    void serializeAndDeserialize_smallObject_shouldNotCompress() {
        // Arrange
        String testString = "Small test string";

        // Act
        byte[] serialized = serializer.serialize(testString);
        Object deserialized = serializer.deserialize(serialized);

        // Assert
        // Check it doesn't have the GZIP marker
        assertThat(serialized.length >= GZIP_MARKER.length).isTrue();
        assertThat(Arrays.equals(Arrays.copyOfRange(serialized, 0, GZIP_MARKER.length), GZIP_MARKER))
                .isFalse();

        // Check we can deserialize correctly
        assertThat(deserialized).isEqualTo(testString);
    }

    @Test
    void serializeAndDeserialize_largeObject_shouldCompress() {
        // Arrange - create a string larger than the threshold (1024 bytes)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("test large object that should be compressed due to its size ");
        }
        String largeString = sb.toString();

        // Act
        byte[] serialized = serializer.serialize(largeString);
        Object deserialized = serializer.deserialize(serialized);

        // Assert
        // Check it has the GZIP marker
        assertThat(serialized.length >= GZIP_MARKER.length).isTrue();
        assertThat(Arrays.equals(Arrays.copyOfRange(serialized, 0, GZIP_MARKER.length), GZIP_MARKER))
                .isTrue();

        // Check we can deserialize correctly
        assertThat(deserialized).isEqualTo(largeString);
    }

    @Test
    void serialize_customObject_shouldWorkCorrectly() {
        // Arrange
        TestObject testObject = new TestObject("test name", 42);

        // Act
        byte[] serialized = serializer.serialize(testObject);
        TestObject deserialized = (TestObject) serializer.deserialize(serialized);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getName()).isEqualTo("test name");
        assertThat(deserialized.getAge()).isEqualTo(42);
    }

    /** Test object for serialization */
    static class TestObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
