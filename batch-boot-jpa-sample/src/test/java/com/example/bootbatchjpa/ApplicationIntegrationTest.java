package com.example.bootbatchjpa;

import com.example.bootbatchjpa.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {}
}