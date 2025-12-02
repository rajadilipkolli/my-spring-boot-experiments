package com.example.ultimateredis.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class RedisCacheGZIPSerializer extends JdkSerializationRedisSerializer {

    // Only compress data larger than 1KB
    private static final int COMPRESSION_THRESHOLD = 1024;

    // Magic bytes to identify compressed data
    private static final byte[] GZIP_MARKER = "GZIP".getBytes(StandardCharsets.UTF_8);

    @Override
    @Nullable
    public Object deserialize(byte @Nullable [] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        // Check if this data is compressed by looking for our marker
        if (bytes.length > GZIP_MARKER.length
                && Arrays.equals(Arrays.copyOfRange(bytes, 0, GZIP_MARKER.length), GZIP_MARKER)) {
            // Remove marker and decompress
            byte[] compressedData = Arrays.copyOfRange(bytes, GZIP_MARKER.length, bytes.length);
            return super.deserialize(decompress(compressedData));
        } else {
            // Not compressed, deserialize directly
            return super.deserialize(bytes);
        }
    }

    @Override
    public byte @Nullable [] serialize(@Nullable Object o) {
        if (o == null) {
            return new byte[0];
        }

        byte[] serialized = super.serialize(o);

        // Only compress if above threshold
        if (serialized.length > COMPRESSION_THRESHOLD) {
            byte[] compressed = compress(serialized);

            // Add marker to identify compressed data
            byte[] result = new byte[GZIP_MARKER.length + compressed.length];
            System.arraycopy(GZIP_MARKER, 0, result, 0, GZIP_MARKER.length);
            System.arraycopy(compressed, 0, result, GZIP_MARKER.length, compressed.length);
            return result;
        } else {
            // Not worth compressing, return original
            return serialized;
        }
    }

    private byte[] compress(byte @Nullable [] data) {
        if (data == null) {
            return new byte[0];
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // compressing the input data using GZIP
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        } catch (IOException e) {
            throw new SerializationException(e.getMessage());
        }

        return byteArrayOutputStream.toByteArray();
    }

    private byte[] decompress(byte @Nullable [] data) {
        if (data == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // decompressing the input data using GZIP
            IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(data)), out);
        } catch (IOException e) {
            throw new SerializationException(e.getMessage());
        }

        return out.toByteArray();
    }
}
