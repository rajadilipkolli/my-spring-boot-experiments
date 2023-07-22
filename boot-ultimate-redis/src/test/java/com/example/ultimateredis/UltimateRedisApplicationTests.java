package com.example.ultimateredis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(classes = TestUltimateRedisApplication.class)
class UltimateRedisApplicationTests {

    @Autowired private RedisTemplate<String, String> redisTemplate;

    @Test
    void contextLoads() {
        assertThat(redisTemplate).isNotNull();
    }
}
