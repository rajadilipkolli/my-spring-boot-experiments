package com.example.ultimateredis.config;

import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RedisCacheGZIPSerializer extends JdkSerializationRedisSerializer {

  @Override
  public Object deserialize(byte[] bytes) {
    return super.deserialize(decompress(bytes));
  }

  @Override
  public byte[] serialize(Object o) {
    return compress(super.serialize(o));
  }

  private byte[] compress(byte[] data) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    // compressing the input data using GZIP
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
      gzipOutputStream.write(data);
    } catch (IOException e) {
      throw new SerializationException(e.getMessage());
    }

    return byteArrayOutputStream.toByteArray();
  }

  private byte[] decompress(byte[] data) {
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
