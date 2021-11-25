package com.example.mongoes;

import com.example.mongoes.mongodb.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MongoDbElasticSearchApplicationTests {

  @Autowired private RestaurantRepository restaurantRepository;

  @AfterEach
  void tearDown() {
    StepVerifier.create(restaurantRepository.deleteAll()).verifyComplete();
  }

  @Test
  void contextLoads() throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);
    StepVerifier.create(restaurantRepository.count())
        .expectNext(1L)
        .expectNextCount(1)
        .verifyComplete();
  }
}
