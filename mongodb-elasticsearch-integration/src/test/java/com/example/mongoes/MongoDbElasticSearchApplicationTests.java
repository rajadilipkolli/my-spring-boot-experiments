package com.example.mongoes;

import com.example.mongoes.elasticsearch.repository.ERestaurantRepository;
import com.example.mongoes.mongodb.repository.RestaurantRepository;
import com.example.mongoes.web.response.ResultData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebTestClient
class MongoDbElasticSearchApplicationTests {

  @Container
  private static final MongoDBContainer MONGO_DB_CONTAINER =
      new MongoDBContainer("mongo:latest").withExposedPorts(27017, 27018, 27019);

  @Container
  static ElasticsearchContainer esContainer =
      new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.2")
          .withEnv("discovery.type", "single-node");

  @DynamicPropertySource
  static void setApplicationProperties(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    propertyRegistry.add("spring.elasticsearch.uris", esContainer::getHttpHostAddress);
  }

  @Autowired private RestaurantRepository restaurantRepository;
  @Autowired private ERestaurantRepository eRestaurantRepository;
  @Autowired private WebTestClient webTestClient;

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

  @Test
  void contextLoads() throws InterruptedException {
    TimeUnit.SECONDS.sleep(5);
    assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
    assertThat(restaurantRepository.count()).isEqualTo(1);

    eRestaurantRepository
        .count()
        .as(StepVerifier::create)
        .consumeNextWith(count -> assertThat(count).isEqualTo(1))
        .verifyComplete();

    Function<UriBuilder, URI> uriFunction =
        uriBuilder ->
            uriBuilder
                .path("/withInRange")
                .queryParam("lat", -73.9)
                .queryParam("lon", 40.8)
                .queryParam("distance", 50)
                .queryParam("unit", "km")
                .build();

    this.webTestClient
        .get()
        .uri(uriFunction)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(String.class)
        .hasSize(1);
  }
}
