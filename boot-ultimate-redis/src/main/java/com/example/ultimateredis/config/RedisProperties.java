package com.example.ultimateredis.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Security security = new Security();

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

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Security {
        private boolean tlsEnabled = false;
        private String aclUsername;

        public boolean isTlsEnabled() {
            return tlsEnabled;
        }

        public void setTlsEnabled(boolean tlsEnabled) {
            this.tlsEnabled = tlsEnabled;
        }

        public String getAclUsername() {
            return aclUsername;
        }

        public void setAclUsername(String aclUsername) {
            this.aclUsername = aclUsername;
        }
    }
}
