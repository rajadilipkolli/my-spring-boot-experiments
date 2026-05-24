package com.example.grpc.spring;

import com.example.grpc.spring.utils.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(AppConstants.PROFILE_TEST)
class SpringGrpcApplicationTests {

    @Test
    void contextLoads() {
    }
}
