package com.example.ultimateredis.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache")
@Data
public class CacheConfigurationProperties {

    private long timeoutSeconds = 60;
    private String redisURI;
    // Mapping of cacheNames to expire-after-write timeout in seconds
    private Map<String, Long> cacheExpirations = new HashMap<>();
}
