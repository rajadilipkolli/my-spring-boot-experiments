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

  private static final MongoDBContainer MONGO_DB_CONTAINER =
      new MongoDBContainer("mongo:latest").withExposedPorts(27017, 27018, 27019);

  @BeforeAll
  static void setUpAll() {
    MONGO_DB_CONTAINER.start();
  }

  @AfterAll
  static void tearDownAll() {
    if (!MONGO_DB_CONTAINER.isShouldBeReused()) {
      MONGO_DB_CONTAINER.stop();
    }
  }

  @AfterEach
  void tearDown() {
    StepVerifier.create(restaurantRepository.deleteAll()).verifyComplete();
  }

  @DynamicPropertySource
  static void setMongoDbContainerURI(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
  }

  @Test
  void contextLoads() throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);
    assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
    StepVerifier.create(restaurantRepository.count())
        .expectNext(1L)
        .expectNextCount(1)
        .verifyComplete();
  }
}
