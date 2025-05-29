package com.example.ultimateredis;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class UltimateRedisApplicationTests extends AbstractIntegrationTest {

    @Autowired private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
        assertThat(redisTemplate).isNotNull();
    }
}
