package com.example.ultimateredis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestUltimateRedisApplication.class)
class UltimateRedisApplicationTests {

    @Test
    void contextLoads() {}
}
