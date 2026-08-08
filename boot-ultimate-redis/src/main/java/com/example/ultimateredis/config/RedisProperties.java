package com.example.ultimateredis.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ultimate.redis")
@Validated
public class RedisProperties {

    @NotBlank
    private String readFrom = "REPLICA_PREFERRED";

    @Positive
    private int gzipThresholdBytes = 1024;

    @NotBlank
    private String keyPrefix = "app:";

    @NotBlank
    private String keyVersion = "v1:";

    public String getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    public int getGzipThresholdBytes() {
        return gzipThresholdBytes;
    }

    public void setGzipThresholdBytes(int gzipThresholdBytes) {
        this.gzipThresholdBytes = gzipThresholdBytes;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }
}
