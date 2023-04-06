package com.example.mongoes;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mongoes.common.AbstractIntegrationTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertThat(ELASTICSEARCH_CONTAINER.isRunning()).isTrue();
    }

    @Test
    @Disabled
    void testWithInRangeEndPoint() {
        Function<UriBuilder, URI> uriFunction =
                uriBuilder ->
                        uriBuilder
                                .path("/search/restaurant/withInRange")
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
