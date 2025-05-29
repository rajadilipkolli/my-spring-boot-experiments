package com.example.ultimateredis.service;

import com.example.ultimateredis.config.RedisValueOperationsUtil;
import com.example.ultimateredis.model.AddRedisRequest;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    private final RedisValueOperationsUtil<String> redisStringUtil;

    public RedisService(RedisValueOperationsUtil<String> redisValueOpsUtil) {
        this.redisStringUtil = redisValueOpsUtil;
    }

    public void addRedis(AddRedisRequest request) {
        log.info("add redis {}", request);
        redisStringUtil.putValue(request.key(), request.value());
        log.info(
                "adding expiry for key {} as : {} minutes", request.key(), request.expireMinutes());
        redisStringUtil.setExpire(request.key(), request.expireMinutes(), TimeUnit.MINUTES);
    }

    public String getValue(String key) {
        log.info("get value {}", key);
        return redisStringUtil.getValue(key);
    }

    public Set<String> getKeysByPattern(String pattern) {
        log.info("getting keys matching pattern: {}", pattern);
        return redisStringUtil.getKeysWithPattern(pattern);
    }

    public void deleteByPattern(String pattern) {
        log.info("deleting keys matching pattern: {}", pattern);
        redisStringUtil.deleteByPattern(pattern);
    }
}
